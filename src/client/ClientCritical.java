package client;

import model.FilterOfClient;

public class ClientCritical extends Client {

    @Override
    protected FilterOfClient createFilter() {
        FilterOfClient filter = new FilterOfClient();
        filter.addLevelFilter("CRITICAL");
        return filter;
    }

    @Override
    protected String getClientDescription() {
        return "This client only receives CRITICAL level alerts";
    }

    public static void main(String[] args)  {
        ClientCritical client = new ClientCritical();
        try {
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}











