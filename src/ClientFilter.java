import java.util.ArrayList;

public class ClientFilter {
    private ArrayList<String> levelFilters;
    private ArrayList<String> sectionFilters;

    public ClientFilter() {
        levelFilters = new ArrayList<>();
        sectionFilters = new ArrayList<>();
    }

    public boolean matches(AlertMessage message) {
        if ((levelFilters.isEmpty() || levelFilters.contains(message.getLevel())) &&
                (sectionFilters.isEmpty() || sectionFilters.contains(message.getSection()))) {
            return true;
        } else {
            return false;
        }
    }

}
