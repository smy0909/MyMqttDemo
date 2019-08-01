package medicalcommunication.com.mymqttdemo;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public class MqttManager {

    public static MqttManager mqttManager;
    String clientId = MqttClient.generateClientId() + "_inbound";
    private MqttConnectOptions conOpt;
    private MqttAndroidClient client;
    private String clientHandle;
    Context context;
    private Connection connection;
    public String USERNAME = "admin";//用户名
    public String PASSWORD = "admin";//密码
    private ActionListener callback;
    public static String PUBLISH_TOPIC = "xbx/nursetable";//发布主题
    MqttConnectListener listener;

    public static MqttManager getInstance(){
        if (mqttManager==null){
            mqttManager=new MqttManager();
        }
        return mqttManager;
    }

    public void setContext(Context context){
        this.context=context;
    }

    public void init(String HOST,int PORT) {
        conOpt = new MqttConnectOptions();

        client = Connections.getInstance(context).createClient(context, HOST +":"+PORT, clientId);

        clientHandle = HOST + PORT + clientId;
        connection = new Connection(clientHandle, clientId, HOST, PORT,
                context, client, false);
        String[] actionArgs = new String[1];
        actionArgs[0] = clientId;
        connection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTING);
        conOpt.setCleanSession(true);
        conOpt.setConnectionTimeout(10);
        conOpt.setKeepAliveInterval(20);
        conOpt.setUserName(USERNAME);
        conOpt.setPassword(PASSWORD.toCharArray());
        callback = new ActionListener(context,
                ActionListener.Action.CONNECT, clientHandle, actionArgs);
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
                callback.onFailure(null, e);
            }
        }
        client.setCallback(new MqttCallbackHandler(context, clientHandle));


        //set traceCallback
        client.setTraceCallback(new MqttTraceCallback());

        connection.addConnectionOptions(conOpt);
        if (client!=null){
            Connections.getInstance(context).addConnection(connection);
        }

    }


    public  void connect(){
        Connections.getInstance(context).getConnection(clientHandle).changeConnectionStatus(Connection.ConnectionStatus.CONNECTING);

        Connection c = Connections.getInstance(context).getConnection(clientHandle);
        c.getClient().setMqttConnectListener(listener);
        try {
            c.getClient().connect(c.getConnectionOptions(), null, new ActionListener(context, ActionListener.Action.CONNECT, clientHandle, null));
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


    public void publish(){
        String[] args = new String[2];
        args[0] = "message";
        args[1] = PUBLISH_TOPIC+";qos:"+1+";retained:"+false;

        try {
            Connections.getInstance(context).getConnection(clientHandle).getClient()
                    .publish(PUBLISH_TOPIC, args[0].getBytes(), 1, false, null, new ActionListener(context, ActionListener.Action.PUBLISH, clientHandle, args));
        }
        catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        }
    }

    public void subscribe() {
        String[] topics = new String[1];
        topics[0] = PUBLISH_TOPIC;
        try {
            Connections.getInstance(context).getConnection(clientHandle).getClient()
                    .subscribe(PUBLISH_TOPIC, 1, null, new ActionListener(context, ActionListener.Action.SUBSCRIBE, clientHandle, topics));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    public void setListener(MqttConnectListener listener){
        this.listener=listener;
    }



}
