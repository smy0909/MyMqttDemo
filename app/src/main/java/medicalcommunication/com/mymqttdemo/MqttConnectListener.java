package medicalcommunication.com.mymqttdemo;

public interface MqttConnectListener {

    public void onSuccess();

    public void onFailure();

    public void onError(String error);
}
