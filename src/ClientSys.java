
public class ClientSys extends Client {

    @Override
    protected FilterOfClient createFilter() {
        FilterOfClient filter = new FilterOfClient();
        filter.addSectionFilter("SYS");
        return filter;
    }

    @Override
    protected String getClientDescription() {
        return "This client only receives alerts from SYS section";
    }

    public static void main(String[] args) throws Exception {
        ClientSys client = new ClientSys();
        client.start();
    }
}