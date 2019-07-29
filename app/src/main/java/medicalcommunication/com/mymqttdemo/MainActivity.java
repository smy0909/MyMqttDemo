package medicalcommunication.com.mymqttdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public class MainActivity extends AppCompatActivity {

    public String HOST = "tcp://106.12.24.200";//服务器地址（协议+地址+端口号）胡云飞本地
    public int PORT = 1883;//服务器地址（协议+地址+端口号）胡云飞本地
    private Connection connection;
    public String USERNAME = "admin";//用户名
    public String PASSWORD = "admin";//密码
    public static String PUBLISH_TOPIC = "app/myqtt";//发布主题
    String clientId = MqttClient.generateClientId() + "_inbound";
    private MqttConnectOptions conOpt;
    private ActionListener callback;
    private String clientHandle;
//    public String HOST = "tcp://101.132.133.252:1883";//服务器地址（协议+地址+端口号）测试

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        conOpt = new MqttConnectOptions();

        client = Connections.getInstance(this).createClient(this, HOST +":"+PORT, clientId);

        clientHandle = HOST + PORT + clientId;
        connection = new Connection(clientHandle, clientId, HOST, PORT,
                this, client, false);
        String[] actionArgs = new String[1];
        actionArgs[0] = clientId;
        connection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTING);
        conOpt.setCleanSession(true);
        conOpt.setConnectionTimeout(10);
        conOpt.setKeepAliveInterval(20);
        conOpt.setUserName(USERNAME);
        conOpt.setPassword(PASSWORD.toCharArray());
        callback = new ActionListener(this,
                ActionListener.Action.CONNECT, clientHandle, actionArgs);
        boolean doConnect = true;
        String message="message";

        if ((!message.equals(ActivityConstants.empty))
                || (!PUBLISH_TOPIC.equals(ActivityConstants.empty))) {
            // need to make a message since last will is set
            try {
                conOpt.setWill(PUBLISH_TOPIC, message.getBytes(), 1,
                        false);
            }
            catch (Exception e) {
                Log.e(this.getClass().getCanonicalName(), "Exception Occured", e);
                doConnect = false;
                callback.onFailure(null, e);
            }
        }
        client.setCallback(new MqttCallbackHandler(this, clientHandle));


        //set traceCallback
        client.setTraceCallback(new MqttTraceCallback());

        connection.addConnectionOptions(conOpt);
        if (client!=null){
            Connections.getInstance(this).addConnection(connection);
        }

    }
    MqttAndroidClient client;

    public void connect(View view) {
        Connections.getInstance(this).getConnection(clientHandle).changeConnectionStatus(Connection.ConnectionStatus.CONNECTING);

        Connection c = Connections.getInstance(this).getConnection(clientHandle);
        try {
            c.getClient().connect(c.getConnectionOptions(), null, new ActionListener(this, ActionListener.Action.CONNECT, clientHandle, null));
        }
        catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to reconnect the client with the handle " + clientHandle, e);
            c.addAction("Client failed to connect");
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to reconnect the client with the handle " + clientHandle, e);
            c.addAction("Client failed to connect");
        }
    }

    public void send(View view) {

        String[] args = new String[2];
        args[0] = "message";
        args[1] = PUBLISH_TOPIC+";qos:"+1+";retained:"+false;

        try {
            Connections.getInstance(this).getConnection(clientHandle).getClient()
                    .publish(PUBLISH_TOPIC, args[0].getBytes(), 1, false, null, new ActionListener(this, ActionListener.Action.PUBLISH, clientHandle, args));
        }
        catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        }
    }
}
