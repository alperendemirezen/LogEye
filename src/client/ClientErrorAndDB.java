package client;

import model.FilterOfClient;

public class ClientErrorAndDB extends Client{

    protected FilterOfClient createFilter() {
        FilterOfClient filter = new FilterOfClient();
        filter.addLevelFilter("ERROR");
        filter.addSectionFilter("DB");
        return filter;
    }

    protected String getClientDescription() {
        return "This client receives ERROR level alerts from DB section";
    }

    public static void main(String[] args) {
        ClientErrorAndDB client = new ClientErrorAndDB();
        try {
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}















