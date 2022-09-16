import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.functors.AndPredicate;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

public class Main {

    static class Record {
        String first;
        String last;
        String school;
        String cohort;

        String[] write() {
            return new String[]{first, last, school, cohort};
        }

        public Record(String first, String last, String school, String cohort) {
            this.first = first;
            this.last = last;
            this.school = school;
            this.cohort = cohort;
        }

        public boolean compatable(Record other) {
            return !other.school.equals(school) && !other.last.equals(last);
        }

        public boolean compatableWithAll(List<Record> others) {
           return others.stream().map(this::compatable).reduce(Boolean.TRUE, Boolean::logicalAnd);
        }
    }

    public static void main(String[] args) throws IOException {
        List<Record> list = new ArrayList<>();
        File file = new File("/Users/drutledge/patrick/Freshman_retreat_groups.csv");
        try (FileReader fileReader = new FileReader(file)) {
            try (CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build()) {
                for (String[] strings : csvReader) {

                    Record record = new Record(strings[0], strings[1], strings[2], strings[3]);
                    list.add(record);
                }
            }
        }

        int group_size = 6;
        List<Record> done_list = new ArrayList<>();
        List<Record> current = new ArrayList<>();
        int cohort_number = 1;

        while (list.size() > group_size) {
            System.out.println(list.size());
            if (current.size() == group_size) {
                cohort_number = finishGroup(done_list, current, cohort_number);
            }

            Optional<Record> option = Optional.empty();
            for (Record record : list) {
                if (record.compatableWithAll(current)) {
                    option = Optional.of(record);
                    list.remove(record);
                    record.cohort = String.valueOf(cohort_number);
                    current.add(record);
                    break;
                }
            }
            if (option.isEmpty()) {
                cohort_number = finishGroup(done_list, current, cohort_number);
            }

        }
        current.forEach(kid -> kid.cohort = "leftover");
        done_list.addAll(current);
        String cohort_num = String.valueOf(cohort_number);
        list.forEach(kid -> kid.cohort = cohort_num);
        done_list.addAll(list);

        File output = new File("/Users/drutledge/patrick/Freshman_retreat_groups_out.csv");
        try (FileWriter fileWriter = new FileWriter(output)) {
            try (CSVWriter csvWriter = new CSVWriter(fileWriter)) {
                csvWriter.writeNext(new String[]{"first", "last", "school", "cohort"});
                done_list.forEach(line -> csvWriter.writeNext(line.write(), false));
            }
        }


    }

    private static int finishGroup(List<Record> done_list, List<Record> current, int cohort_number) {
        done_list.addAll(current);
        current.clear();
        cohort_number++;
        return cohort_number;
    }
}
