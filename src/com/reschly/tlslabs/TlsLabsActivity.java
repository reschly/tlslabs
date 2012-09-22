package com.reschly.tlslabs;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.Toast;

public class TlsLabsActivity extends Activity implements OnClickListener {
	
	private EditText resultsBox;
	private EditText hostBox;
	private EditText portBox;
	private Button submit;
	private Toast toast;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        resultsBox = (EditText) findViewById(R.id.results_box);
        resultsBox.setEnabled(false);
        hostBox = (EditText) findViewById(R.id.host_field);
        portBox = (EditText) findViewById(R.id.port_field);
        submit = (Button) findViewById(R.id.submit_button);
        submit.setOnClickListener(this);
        
        resultsBox.setScroller(new Scroller(this));
        resultsBox.setVerticalScrollBarEnabled(true);
        resultsBox.setHorizontalScrollBarEnabled(true);
        resultsBox.setHorizontallyScrolling(true);
        resultsBox.setMovementMethod(new ScrollingMovementMethod());
                
        toast = Toast.makeText(this, null, Toast.LENGTH_LONG);
    }
    
	@Override
	public void onClick(View v)
	{		
		int id = v.getId();
		switch(id)
		{
			case R.id.submit_button:
				String host = hostBox.getText().toString();
				if (TextUtils.isEmpty(host))
				{
					toast.setText("Empty hostname");
					return;
				}
				int port;
				try {
					port = Integer.parseInt(portBox.getText().toString());
				}
				catch (NumberFormatException e)
				{
					toast.setText("Bad Number Format");
					toast.show();
					return;
				}
				if ((port > 65535) || (port < 0))
				{
					toast.setText("Port out of range");
					toast.show();
					return;
				}
				hostBox.setEnabled(false);
				portBox.setEnabled(false);
				submit.setEnabled(false);
				
				toast.setText("Scanning...");
				toast.show();
				new TlsScan(host, port).execute();
								
				break;
			default:
				
		}
		
	}
	
	private class TlsScan extends AsyncTask<Void, String, String> 
	{
		String host;
		int port;
		String scanResults;
		
		public TlsScan(String host, int port)
		{
			super();
			this.host = host;
			this.port = port;
		}
		

		@Override
		protected String doInBackground(Void... args)
		{
			String result = "scan successful";
			
			try
			{
				scanResults = TlsScanner.doScan(host, port);
			}
			catch (Exception e)
			{
				result = e.getMessage();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result)
		{
			toast.setText(result);
			toast.show();
			resultsBox.setText(scanResults);
			hostBox.setEnabled(true);
			portBox.setEnabled(true);
			submit.setEnabled(true);
		}
		
	}
}