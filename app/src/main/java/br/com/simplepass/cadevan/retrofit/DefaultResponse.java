package br.com.simplepass.cadevan.retrofit;

/**
 * Created by leandro on 3/7/16.
 */
public class DefaultResponse extends BaseResponse {
    private String serverResponse;

    public String getServerResponse() {
        return serverResponse;
    }

    public void setServerResponse(String serverResponse) {
        this.serverResponse = serverResponse;
    }
}
