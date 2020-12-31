package eu.johannes.runninggag;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.TableDataAdapter;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

import static eu.johannes.runninggag.Reversed.reversed;

public class CategoryListFragment extends Fragment {
    private static final int[] TABLE_HEADERS = {R.string.date, R.string.distance, R.string.time};

    private OnlyOneRunViewModel viewModel;
    private SortableTableView<OnlyOneRun> tableView;
    private RunningGagData runningGagData;
    public static final int LIMIT = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.categorylist, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(OnlyOneRunViewModel.class);

        runningGagData = RunningGagData.loadData(getActivity());

        tableView = (SortableTableView<OnlyOneRun>) v.findViewById(R.id.categoriesTable);
        tableView.setColumnCount(TABLE_HEADERS.length);

        TableColumnWeightModel columnModel = new TableColumnWeightModel(TABLE_HEADERS.length);
        columnModel.setColumnWeight(0, 2);
        columnModel.setColumnWeight(2, 2);
        tableView.setColumnModel(columnModel);

        setRunlistAdapter();
        tableView.setColumnComparator(0, (o1, o2) -> -Long.compare(o1.getStartTime(), o2.getStartTime()));
        tableView.setColumnComparator(1, (o1, o2) -> -Double.compare(o1.getDistance(), o2.getDistance()));
        tableView.setColumnComparator(2, (o1, o2) -> -Long.compare(o1.caculateTotalRunTime(), o2.caculateTotalRunTime()));

        SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(getActivity(), TABLE_HEADERS);
        simpleTableHeaderAdapter.setPaddingLeft(0);
        tableView.setHeaderAdapter(simpleTableHeaderAdapter);
    }

    private void setRunlistAdapter() {
        int category = getRun().getCategory();
        ArrayList<OnlyOneRun> runs = new ArrayList<>();
        runs.add(getRun());
        for (OnlyOneRun run : reversed(runningGagData.getRuns())) {
            if (run.getCategory() == category && !run.equals(getRun())) {
                runs.add(0, run);
                if (runs.size() >= LIMIT) {
                    break;
                }
            }
        }

        tableView.setDataAdapter(new RunTableDataAdapter(getActivity(), runs));
    }

    public class RunTableDataAdapter extends TableDataAdapter<OnlyOneRun> {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        DecimalFormat f = new DecimalFormat("#0.00");

        public RunTableDataAdapter(Context context, List<OnlyOneRun> data) {
            super(context, data);
        }

        @Override
        public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
            OnlyOneRun run = getRowData(rowIndex);
            TextView textView = (TextView) View.inflate(getActivity(), R.layout.list_black_textview, null);
            textView.setPadding(textView.getPaddingLeft(), 10, textView.getPaddingRight(), 10);
            if (run == getRun()) {
                textView.setTextColor(getResources().getColor(R.color.colorGreen));
            }
            String content = "Hae??";
            switch (columnIndex) {
                case 0:
                    content = df.format(new Date(run.getStartTime()));
                    break;
                case 1:
                    textView.setGravity(Gravity.RIGHT);
                    content = f.format(run.getDistance() / 1000d) + " km ";
                    break;
                case 2:
                    content = Runnow.getDurationString(run.caculateTotalRunTime() / 1000l);
                    break;
            }
            textView.setText(content);
            return textView;
        }
    }

    public OnlyOneRun getRun() {
        return viewModel.getSelectedOnlyOneRun().getValue();
    }
}
