package com.l.csorlo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class LedFragment extends Fragment {

    SeekBar[] seekBars = new SeekBar[SharedViewModel.SIZE_LED];
    Switch[] switches = new Switch[SharedViewModel.SIZE_LED];
    LinearLayout linearLayout;
    private SharedViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.led_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.button).setOnClickListener(view1 -> NavHostFragment.findNavController(LedFragment.this)
                .navigate(R.id.action_ledFragment_to_FirstFragment));

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel.isConnected().observe(getViewLifecycleOwner(), aBoolean -> onResume());
        linearLayout = view.findViewById(R.id.linearLayoutSeek);

        seekBars[0] = view.findViewById(R.id.seekBar0);
        seekBars[1] = view.findViewById(R.id.seekBar1);
        seekBars[2] = view.findViewById(R.id.seekBar2);
        seekBars[3] = view.findViewById(R.id.seekBar3);
        seekBars[4] = view.findViewById(R.id.seekBar4);
        seekBars[5] = view.findViewById(R.id.seekBar5);
        for (int i = 0; i < seekBars.length; i++) {
            int fi = i;
            seekBars[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser)
                        viewModel.setLED(fi, progress);
                    else
                        switches[fi].setChecked(progress != 0);
                    switches[fi].setText(String.format(Locale.getDefault(),"%d",progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }


        switches[0] = view.findViewById(R.id.switchLed0);
        switches[1] = view.findViewById(R.id.switchLed1);
        switches[2] = view.findViewById(R.id.switchLed2);
        switches[3] = view.findViewById(R.id.switchLed3);
        switches[4] = view.findViewById(R.id.switchLed4);
        switches[5] = view.findViewById(R.id.switchLed5);
        for (int i = 0; i < switches.length; i++) {
            int fi = i;
            switches[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                seekBars[fi].setEnabled(isChecked);
                viewModel.setSwitchIsChecked(fi, isChecked);
                if (isChecked)
                    viewModel.setLED(fi, seekBars[fi].getProgress());
                else
                    viewModel.setLED(fi, 0);
            });
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        System.out.println("pause");
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean isCon = viewModel.isConnected().getValue();
        for (int i = 0; i < SharedViewModel.SIZE_LED; i++) {
            if (isCon)
                seekBars[i].setProgress(viewModel.getLed(i));
            switches[i].setChecked(viewModel.getSwitchIsChecked(i));
            seekBars[i].setEnabled(isCon && switches[i].isChecked());
            switches[i].setEnabled(isCon);
            //linearLayout.setVisibility(isCon?View.VISIBLE:View.INVISIBLE);
        }
    }
}