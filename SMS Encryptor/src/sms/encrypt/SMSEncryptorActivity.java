package sms.encrypt;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;


import android.R.string;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.telephony.gsm.SmsManager;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class SMSEncryptorActivity extends Activity
{
	public ImageButton contact;
	public Button send;
	public Button inbox;
	private static final int PICK_CONTACT = 1;
	public String name = null;
	public String number = null;
	public EditText etnumber;
	public EditText etmessage;
	
	// Change the password here or give a user possibility to change it
    public static final byte[] PASSWORD = new byte[]{ 0x20, 0x32, 0x34, 0x47, (byte) 0x84, 0x33, 0x58 };
    
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
		this.setContentView(R.layout.sms);
		
		etnumber= (EditText) findViewById(R.id.to);
		etmessage= (EditText) findViewById(R.id.message);
		
		contact = (ImageButton) findViewById(R.id.contact);
		contact.setOnClickListener(handlecontact);
		
		send = (Button) findViewById(R.id.send);
		send.setOnClickListener(handlesend);
	}
	private final OnClickListener handlecontact = new OnClickListener() {
		
		public void onClick(View v) {
			
			Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
	        startActivityForResult(intent, PICK_CONTACT);
			
		}
	};
	
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
    	  super.onActivityResult(reqCode, resultCode, data);

    	  switch (reqCode) {
    	    case (PICK_CONTACT) :
    	      if (resultCode == Activity.RESULT_OK) {
    	    	  Uri contactData = data.getData();
    	    	  Cursor c =  managedQuery(contactData, null, null, null, null);
    	    	  
    	    	  if (c.moveToFirst()) {
    	    		  
    	    		 String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

 	    	         String hasPhone =c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

 	    	          if (hasPhone.equalsIgnoreCase("1")) {
 	    	        	   Cursor phones = getContentResolver().query( 
 	    	                       ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, 
 	    	                       ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id, 
 	    	                       null, null);
 	    	             phones.moveToFirst();
 	    	             number = phones.getString(phones.getColumnIndex("data1"));
 	    	          }
 	    	         name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
    	    		  etnumber.setText(name);
    	    	  }
    	      	}
    	      	break;
    	  }
    	}
    
    private final OnClickListener handlesend = new OnClickListener() {
		
		public void onClick(View v) {
			
			String msg = etmessage.getText().toString(); 
			String str = etnumber.getText().toString();
            if (number.length()>0 && msg.length()>0) {               
            	try {
					msg = StringCryptor.encrypt("PASSWORD", msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	sendSMS(number, msg);                
            }
            else if(str.length()>0 && msg.length()>0)
            {
            	try {
					msg = StringCryptor.encrypt("PASSWORD", msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	sendSMS(str, msg);
            }
            else
            	Toast.makeText(getBaseContext(), 
                    "Please enter both phone number and message.", 
                    Toast.LENGTH_SHORT).show();
        }
			
		
	};
	

	
	//---sends a SMS message to another device---
    private void sendSMS(String phoneNumber, String message)
    {      
    	String SENT = "SMS_SENT";
    	String DELIVERED = "SMS_DELIVERED";
    	
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
            new Intent(SENT), 0);
        
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
            new Intent(DELIVERED), 0);
    	
        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode())
				{
				    case Activity.RESULT_OK:
					    Toast.makeText(getBaseContext(), "SMS sent", 
					    		Toast.LENGTH_SHORT).show();
					    break;
				    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					    Toast.makeText(getBaseContext(), "Generic failure", 
					    		Toast.LENGTH_SHORT).show();
					    break;
				    case SmsManager.RESULT_ERROR_NO_SERVICE:
					    Toast.makeText(getBaseContext(), "No service", 
					    		Toast.LENGTH_SHORT).show();
					    break;
				    case SmsManager.RESULT_ERROR_NULL_PDU:
					    Toast.makeText(getBaseContext(), "Null PDU", 
					    		Toast.LENGTH_SHORT).show();
					    break;
				    case SmsManager.RESULT_ERROR_RADIO_OFF:
					    Toast.makeText(getBaseContext(), "Radio off", 
					    		Toast.LENGTH_SHORT).show();
					    break;
				}
			}
        }, new IntentFilter(SENT));
        
        //---when the SMS has been delivered---
//        registerReceiver(new BroadcastReceiver(){
//			@Override
//			public void onReceive(Context arg0, Intent arg1) {
//				switch (getResultCode())
//				{
//				    case Activity.RESULT_OK:
//					    Toast.makeText(getBaseContext(), "SMS delivered", 
//					    		Toast.LENGTH_SHORT).show();
//					    break;
//				    case Activity.RESULT_CANCELED:
//					    Toast.makeText(getBaseContext(), "SMS not delivered", 
//					    		Toast.LENGTH_SHORT).show();
//					    break;					    
//				}
//			}
//        }, new IntentFilter(DELIVERED));        
    	
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);               
    }    
}
