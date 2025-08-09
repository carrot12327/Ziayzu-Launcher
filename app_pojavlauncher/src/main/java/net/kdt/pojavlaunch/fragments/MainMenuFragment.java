package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.Tools.openPath;
import static net.kdt.pojavlaunch.Tools.shareLog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.mcVersionSpinner;

import net.kdt.pojavlaunch.CustomControlsActivity;
import git.artdeell.mojo.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.instances.InstanceManager;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;

import java.io.File;

public class MainMenuFragment extends Fragment {
    public static final String TAG = "MainMenuFragment";

    private mcVersionSpinner mVersionSpinner;

    public MainMenuFragment(){
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button mNewsButton = view.findViewById(R.id.news_button);
        Button mDiscordButton = view.findViewById(R.id.discord_button);
        Button mCustomControlButton = view.findViewById(R.id.custom_control_button);
        Button mInstallJarButton = view.findViewById(R.id.install_jar_button);
        Button mShareLogsButton = view.findViewById(R.id.share_logs_button);
        Button mOpenDirectoryButton = view.findViewById(R.id.open_files_button);

        ImageButton mEditProfileButton = view.findViewById(R.id.edit_profile_button);
        Button mPlayButton = view.findViewById(R.id.play_button);
        mVersionSpinner = view.findViewById(R.id.mc_version_spinner);

        mNewsButton.setOnClickListener(v -> Tools.openURL(requireActivity(), Tools.URL_HOME));
        mDiscordButton.setOnClickListener(v -> Tools.openURL(requireActivity(), getString(R.string.discord_invite)));
        mCustomControlButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), CustomControlsActivity.class)));
        mInstallJarButton.setOnClickListener(v -> runInstallerWithConfirmation(false));
        mInstallJarButton.setOnLongClickListener(v->{
            runInstallerWithConfirmation(true);
            return true;
        });
        mEditProfileButton.setOnClickListener(v -> mVersionSpinner.openProfileEditor(requireActivity()));

        mPlayButton.setOnClickListener(v -> ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true));

        mShareLogsButton.setOnClickListener((v) -> shareLog(requireContext()));

        mOpenDirectoryButton.setOnClickListener((v)-> openPath(v.getContext(), getCurrentProfileDirectory(), false));

        mNewsButton.setOnLongClickListener((v)->{
            Tools.swapFragment(requireActivity(), GamepadMapperFragment.class, GamepadMapperFragment.TAG, null);
            return true;
        });

        // Header actions
        TextView addAccount = view.findViewById(R.id.btn_account1);
        ImageButton settings = view.findViewById(R.id.btn_settings);
        if (addAccount != null) {
            addAccount.setOnClickListener(v -> ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true));
        }
        if (settings != null) {
            settings.setOnClickListener(v -> Tools.swapFragment(requireActivity(), LauncherPreferenceFragment.class, LauncherPreferenceFragment.class.getSimpleName(), null));
        }

        // Footer arrows
        ImageButton up = view.findViewById(R.id.btn_footer_up);
        ImageButton diag = view.findViewById(R.id.btn_footer_diag);
        ScrollView scroll = view.findViewById(R.id.main_scroll);
        if (up != null && scroll != null) {
            up.setOnClickListener(v -> scroll.smoothScrollTo(0, 0));
        }
        if (diag != null) {
            diag.setOnClickListener(v -> openPath(v.getContext(), getCurrentProfileDirectory(), false));
        }

        // Start overlay animations
        try {
            ImageView orb1 = view.findViewById(R.id.orb1);
            ImageView orb2 = view.findViewById(R.id.orb2);
            ImageView p1 = view.findViewById(R.id.particle1);
            ImageView p2 = view.findViewById(R.id.particle2);
            Animation floatSlow = AnimationUtils.loadAnimation(requireContext(), R.anim.float_slow);
            Animation floatParticle = AnimationUtils.loadAnimation(requireContext(), R.anim.float_particle);
            if (orb1 != null) orb1.startAnimation(floatSlow);
            if (orb2 != null) orb2.startAnimation(floatSlow);
            if (p1 != null) p1.startAnimation(floatParticle);
            if (p2 != null) p2.startAnimation(floatParticle);
        } catch (Exception ignored) {}

        // Start header shimmer sweep
        View shimmer = view.findViewById(R.id.header_shimmer);
        if (shimmer != null) {
            shimmer.setAlpha(1f);
            shimmer.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.shimmer_sweep));
        }
    }

    private File getCurrentProfileDirectory() {
        return InstanceManager.getSelectedListedInstance().getGameDirectory();
    }

    @Override
    public void onResume() {
        super.onResume();
        ExtraCore.setValue(ExtraConstants.REFRESH_ACCOUNT_SPINNER, true);
    }

    private void runInstallerWithConfirmation(boolean isCustomArgs) {
        if (ProgressKeeper.getTaskCount() == 0)
            Tools.installMod(requireActivity(), isCustomArgs);
        else
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
    }
}
