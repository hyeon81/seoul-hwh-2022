package com.devfive.shw2022;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<Holder> {
    ArrayList<DataModel> list;
    TextView total;
    Button purchase;
    Button reset;
    int sum = 0;

    CustomAdapter(ArrayList<DataModel> list, TextView total, Button purchase, Button reset) {
        this.list = list;
        this.total = total;
        this.purchase = purchase;
        this.reset = reset;
    }

    public void removeData(int pos) {
        this.list.remove(pos);
        noti();
    }

    public void clearData() {
        this.list.clear();
        noti();
    }

    private void noti(){
        sum = 0;
        for (DataModel dataModel : list) {
            sum += dataModel.cnt * dataModel.price;
        }
        total.setText(sum + "");
        notifyDataSetChanged();
        if (sum > 0)
        {
            purchase.setEnabled(true);
            reset.setEnabled(true);
        }
        else {
            purchase.setEnabled(false);
            reset.setEnabled(false);
        }
    }

    public void addData(String name, int price) {

        boolean b = true;
        for (DataModel dataModel : this.list) {
            if (dataModel.name.equals(name)) {
                dataModel.cnt++;
                b = false;
            }
        }
        if (b)
            this.list.add(new DataModel(name, price));
        noti();
    }

    public void addBtn(int pos) {
        this.list.get(pos).cnt++;
        noti();
    }

    public int subBtn(int pos) {
        this.list.get(pos).cnt--;
        noti();
        return this.list.get(pos).cnt;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.itemview, parent, false);
        return new Holder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.name.setText(list.get(position).name);
        holder.price.setText(list.get(position).price + "");
        holder.cnt.setText(list.get(position).cnt + "");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

class Holder extends RecyclerView.ViewHolder {
    TextView name;
    TextView price;
    Button btn_delete;
    Button add;
    Button sub;
    TextView cnt;

    public Holder(@NonNull View itemView, CustomAdapter adapter) {
        super(itemView);

        name = itemView.findViewById(R.id.name);
        price = itemView.findViewById(R.id.price);
        btn_delete = itemView.findViewById(R.id.delete);
        add = itemView.findViewById(R.id.add);
        sub = itemView.findViewById(R.id.sub);
        cnt = itemView.findViewById(R.id.cnt);

        add.setOnClickListener(v -> {
            sub.setEnabled(true);
            adapter.addBtn(getAdapterPosition());
        });
        sub.setOnClickListener(v -> {
            if (adapter.subBtn(getAdapterPosition()) == 1)
                sub.setEnabled(false);
            else
                sub.setEnabled(true);
        });

        btn_delete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext())
                    .setMessage("삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        adapter.removeData(getAdapterPosition());
                    })
                    .setNegativeButton("취소", (dialog, which) -> {
                        dialog.dismiss();
                    });
            AlertDialog msgDlg = builder.create();
            msgDlg.show();
        });
    }

}
