package com.uth.exa1pa1199.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.uth.exa1pa1199.R;
import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;
import com.uth.exa1pa1199.util.FechaUtils;

import java.util.Objects;

public class RegistroAdapter extends ListAdapter<RegistroResumenDto, RegistroAdapter.ViewHolder> {
    public interface OnRegistroClick {
        void onClick(RegistroResumenDto registro);
    }

    private final OnRegistroClick listener;

    public RegistroAdapter(OnRegistroClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_registro, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RegistroResumenDto r = getItem(position);
        holder.titulo.setText(r.getTitulo());
        holder.descripcion.setText(r.getDescripcion());
        holder.fecha.setText(FechaUtils.formatear(r.getFechaCreacion()));
        holder.foto.setText(r.isTieneFoto() ? R.string.con_foto : R.string.sin_foto);
        holder.itemView.setOnClickListener(v -> listener.onClick(r));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView titulo, descripcion, fecha, foto;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.textTitulo);
            descripcion = itemView.findViewById(R.id.textDescripcion);
            fecha = itemView.findViewById(R.id.textFecha);
            foto = itemView.findViewById(R.id.textFoto);
        }
    }

    private static final DiffUtil.ItemCallback<RegistroResumenDto> DIFF = new DiffUtil.ItemCallback<RegistroResumenDto>() {
        @Override
        public boolean areItemsTheSame(@NonNull RegistroResumenDto a, @NonNull RegistroResumenDto b) {
            return Objects.equals(a.getId(), b.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull RegistroResumenDto a, @NonNull RegistroResumenDto b) {
            return Objects.equals(a.getTitulo(), b.getTitulo())
                    && Objects.equals(a.getDescripcion(), b.getDescripcion())
                    && Objects.equals(a.getFechaCreacion(), b.getFechaCreacion())
                    && a.isTieneFoto() == b.isTieneFoto();
        }
    };
}
