package com.example.pokeapi_v2;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.ViewHolder> {

    private ArrayList<Pokemon> lista;
    private ArrayList<Pokemon> seleccionados;

    public PokemonAdapter(ArrayList<Pokemon> lista, ArrayList<Pokemon> seleccionados) {
        this.lista = lista;
        this.seleccionados = seleccionados;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt;

        public ViewHolder(View v) {
            super(v);
            txt = v.findViewById(R.id.txtPokemon);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pokemon, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pokemon p = lista.get(position);

        holder.txt.setText(
                p.name +
                        " ❤️" + p.hp +
                        " ⚔️" + p.attack +
                        " 🔥" + (p.hp + p.attack)
        );

        // Pintar selección
        if (seleccionados.contains(p)) {
            holder.txt.setBackgroundColor(Color.GREEN);
        } else {
            holder.txt.setBackgroundColor(Color.LTGRAY);
        }

        holder.itemView.setOnClickListener(v -> {
            if (seleccionados.contains(p)) {
                seleccionados.remove(p);
            } else {
                if (seleccionados.size() < 3) {
                    seleccionados.add(p);
                }
            }
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}