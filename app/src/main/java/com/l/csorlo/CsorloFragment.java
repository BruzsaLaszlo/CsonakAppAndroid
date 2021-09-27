package com.l.csorlo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class CsorloFragment extends Fragment {

    SharedViewModel viewModel;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_csorlo, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        Button buttonToLed = view.findViewById(R.id.button_to_led);
        buttonToLed.setOnClickListener(view1 -> NavHostFragment.findNavController(CsorloFragment.this)
                .navigate(R.id.action_FirstFragment_to_ledFragment));

        view.findViewById(R.id.buttonMaps).setOnClickListener(view1 -> NavHostFragment.findNavController(CsorloFragment.this)
                .navigate(R.id.action_FirstFragment_to_mapsFragment2));

        EditText log = view.findViewById(R.id.textMultiLine);
        Button buttonElore = view.findViewById(R.id.buttonElore);
        Button buttonHatra = view.findViewById(R.id.buttonHatra);
        Button buttonStop = view.findViewById(R.id.buttonStop);

        buttonElore.setOnClickListener((b) -> viewModel.setCsorlo(1));
        buttonHatra.setOnClickListener((b) -> viewModel.setCsorlo(2));
        buttonStop.setOnClickListener((b) -> viewModel.setCsorlo(0));

        viewModel.getCsorlo().observe(getViewLifecycleOwner(), csorlo ->{
            if (csorlo == 0){
                buttonStop.setEnabled(false);
                buttonElore.setEnabled(true);
                buttonHatra.setEnabled(true);
            } else if (csorlo == 1){
                buttonHatra.setEnabled(true);
                buttonElore.setEnabled(false);
                buttonStop.setEnabled(true);
            }else if (csorlo == 2){
                buttonHatra.setEnabled(false);
                buttonElore.setEnabled(true);
                buttonStop.setEnabled(true);
            }
        });
        viewModel.getLog().observe(getViewLifecycleOwner(), log::setText);


    }
}