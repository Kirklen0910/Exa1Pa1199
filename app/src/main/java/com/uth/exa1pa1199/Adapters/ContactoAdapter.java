package com.uth.exa1pa1199.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.uth.exa1pa1199.Database.ContactoDbHelper;
import com.uth.exa1pa1199.Models.Contacto;
import com.uth.exa1pa1199.Views.MainActivity;
import com.uth.exa1pa1199.R;

import java.util.List;

public class ContactoAdapter extends BaseAdapter {
    private final Context context;
    private final List<Contacto> contactos;
    private final ContactoDbHelper dbHelper;

    public ContactoAdapter(Context context, List<Contacto> contactos) {
        this.context = context;
        this.contactos = contactos;
        this.dbHelper = new ContactoDbHelper(context);
    }

    @Override
    public int getCount() { return contactos.size(); }

    @Override
    public Object getItem(int position) { return contactos.get(position); }

    @Override
    public long getItemId(int position) { return contactos.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_contacto, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contacto contacto = contactos.get(position);
        holder.textNombre.setText(contacto.getNombre());
        holder.textTelefono.setText(contacto.getCodigoArea() + contacto.getTelefono());

        if (contacto.getImagenUri() != null) {
            holder.imageContacto.setImageURI(Uri.parse(contacto.getImagenUri()));
        } else {
            holder.imageContacto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        convertView.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_contacto, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_editar) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_CONTACTO_ID, contacto.getId());
                    context.startActivity(intent);
                    return true;
                } else if (itemId == R.id.menu_eliminar) {
                    new AlertDialog.Builder(context)
                            .setTitle("Eliminar contacto")
                            .setMessage("¿Seguro que deseas eliminar este contacto?")
                            .setPositiveButton("Sí", (dialog, which) -> {
                                dbHelper.eliminarContacto(contacto.getId());
                                contactos.remove(position);
                                notifyDataSetChanged();
                                Toast.makeText(context, "Contacto eliminado", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("No", null)
                            .show();
                    return true;
                } else if (itemId == R.id.menu_compartir) {
                    String numeroCompleto = contacto.getCodigoArea() + contacto.getTelefono();
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT,
                            "Contacto: " + contacto.getNombre() + "\nTel: " + numeroCompleto);
                    context.startActivity(Intent.createChooser(shareIntent, "Compartir contacto"));
                    return true;
                } else if (itemId == R.id.menu_llamar) {
                    String numeroCompleto = contacto.getCodigoArea() + contacto.getTelefono();
                    new AlertDialog.Builder(context)
                            .setTitle("Acción")
                            .setMessage("¿Desea llamar a " + contacto.getNombre() + "?")
                            .setPositiveButton("Sí", (dialog, which) -> {
                                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                                dialIntent.setData(Uri.parse("tel:" + numeroCompleto));
                                context.startActivity(dialIntent);
                            })
                            .setNegativeButton("No", null)
                            .show();
                    return true;
                }
                return false;
            });
            popup.show();
        });

        return convertView;
    }

    static class ViewHolder {
        ImageView imageContacto;
        TextView textNombre, textTelefono;

        ViewHolder(View view) {
            imageContacto = view.findViewById(R.id.imageContacto);
            textNombre = view.findViewById(R.id.textNombre);
            textTelefono = view.findViewById(R.id.textTelefono);
        }
    }
}
