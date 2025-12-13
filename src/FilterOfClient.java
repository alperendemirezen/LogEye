import java.util.ArrayList;
import java.util.List;

public class FilterOfClient {
    private List<String> levels;
    private List<String> sections;

    public FilterOfClient() {
        this.levels = new ArrayList<>();
        this.sections = new ArrayList<>();
    }

    public List<String> getLevels() {
        return levels;
    }

    public List<String> getSections() {
        return sections;
    }

    public void addLevelFilter(String level) {
        levels.add(level);
    }

    public void addSectionFilter(String section) {
        sections.add(section);
    }

    public boolean matches(AlertMessage message) {
        boolean levelMatch = levels.isEmpty() || levels.contains(message.getLevel());
        boolean sectionMatch = sections.isEmpty() || sections.contains(message.getSection());

        return levelMatch && sectionMatch;
    }
}