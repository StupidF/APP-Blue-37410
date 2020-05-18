package com.example.blte;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;


import android.content.res.AssetManager;
import android.media.SoundPool;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.example.blte.DeviceListActivity;

public class MainActivity extends Activity {
	private SoundPool mSoundPool = null;
	private HashMap<Integer, Integer> soundID = new HashMap<Integer, Integer>();
	private AssetManager aManager;
    Button butblte=null;//����һ�������ؼ�,��������������



	EditText Angle=null;//ͨ��1��ʾ

    boolean enable=false;
    boolean blecon=true;
    boolean bThread=false;
	private List<String> mBuffer;//����һ����������
    String showstr="";
	private InputStream inputStream;    //������������������������
	BluetoothSocket socket = null; // ����ͨ��socket
	BluetoothDevice device = null;
	BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();//��ȡ����
	private final static int REQUEST_CONNECT_DEVICE = 1;    //�궨���ѯ�豸���
	private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        butblte  = (Button)findViewById(R.id.BUTBLTE);//�������ư���

		// ͼƬ
		Angle = (EditText)findViewById(R.id.angle);//��
        butblte.setOnClickListener(new setclick());//���尴ť�¼�

       if(adapter.isEnabled())//��������
        	enable=true;//������������
      }

    /*
     *
     * i
     */
    public class setclick implements OnClickListener
    {

		@Override
		public void onClick(View arg0) {
			if(adapter!=null)
			{
		    	if(!adapter.isEnabled())
		    	{
		    		Toast.makeText(MainActivity.this,"��������"	, 0).show();
					adapter.enable();//����������
		    	}
			}
			else
			{
				Toast.makeText(MainActivity.this,"����������"	, 0).show();
			}
			if(!enable)//�������ʧ�ܣ���Ҫ��������
				blecon=true;
			new BLEThread().start();// ���������ֳ�
   		 }
   }

    /*
     * 
     */
    Handler mHandler = new Handler() {  //�ȴ������������ص�һЩ������Ϣ
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String a, LED1status, LED2status,LED3status;
			switch (msg.what) {
			case 1:
				new BLEInput().start();// �������������߳�
				break;
			case 2:
				String result = msg.getData().get("msg").toString();
				showstr = showstr + result;
				if (showstr.length() >=7)// ���յ������ݴ���7����������������
				{
					a = showstr.substring(0, 1);

					if (a.equals("T"))// java�ַ�����0��ʼ,�����һ���ַ���T���������յ�����������ȷ��
					{
						Angle.setText(showstr.substring(1,7));


					}
					showstr = "";
				}
				break;
			default:
				break;
			}
		}
    };

    //����һ���߳� ,�̲߳��������UI  
    public class BLEThread extends Thread
    {
		 public void run()
		 {
			 while(blecon)
			 {
			  if(adapter.isEnabled())//�������ã�
			  {
				 enable=true;
				 blecon=false;//ֻ���м��һ��
			  }
				 if(enable)//����������������
				 {
					 Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class); // ��ת��������
					 startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); // ���÷��غ궨��
					 enable=false;
				 }
			 }
		 }
    }
 // ���ջ�������ӦstartActivityForResult() ��׿�ص�����
 	public void onActivityResult(int requestCode, int resultCode, Intent data)
 	{

 		switch(requestCode)
 	  {
 		case REQUEST_CONNECT_DEVICE:
 		if (resultCode == Activity.RESULT_OK) //���������������������
 		{
            String address = data.getExtras()
                                 .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            // �õ������豸���
            device = adapter.getRemoteDevice(address);//Զ�˵�
            try {
				socket= device.createRfcommSocketToServiceRecord(UUID
						.fromString(SPP_UUID));//ͨ��socket�����Ƿ����ӳɹ�
			} catch (IOException e) {
				Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
			}
            try {
				socket.connect();
				butblte.setText("���ӳɹ�");
				Toast.makeText(this, "����"+device.getName()+"�ɹ���", Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();

					e.printStackTrace();
			}
        	try {
				inputStream  = socket.getInputStream();
				  new BLEInput().start();//�������������߳�
			} catch (IOException e) {
				e.printStackTrace();
			}   //�õ���������������
		}
 		break;
 		default:break;
 	  }
 	}

	/*
	 * �����߳��������ݽ���ʹ��
	 */
	// ����һ���߳� ,�̲߳��������UI
	public class BLEInput extends Thread {
		String str;
		// �õ�һ����Ϣ����Message������Android����ϵͳ�ṩ
		int num;

		public void run() {
			while (true) {

				byte buffer[] = new byte[1024];// ����1024���ֽ�
				try {
					num = inputStream.read(buffer);
					str = new String(buffer, 0, num);
					Message msg = new Message();
					msg.what = 2;
					Bundle data = new Bundle();
					data.putString("msg", str);
					msg.setData(data);
					mHandler.sendMessage(msg);// �������ݸ�handler������������ݸ���*/
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}



