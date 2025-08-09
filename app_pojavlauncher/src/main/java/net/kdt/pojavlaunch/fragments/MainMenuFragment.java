package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.Tools.openPath;
import static net.kdt.pojavlaunch.Tools.shareLog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.io.File;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.content.SharedPreferences;
import androidx.core.content.ContextCompat;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;

public class MainMenuFragment extends Fragment {
    public static final String TAG = "MainMenuFragment";

    private mcVersionSpinner mVersionSpinner;
    private View mRootView;
    private SharedPreferences.OnSharedPreferenceChangeListener mAnimatedThemePrefListener;
    private View[] mHomeButtons;

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

        mHomeButtons = new View[]{
                mNewsButton, mDiscordButton, mCustomControlButton,
                mInstallJarButton, mShareLogsButton, mOpenDirectoryButton,
                mPlayButton
        };

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

        // Prepare animated background root view
        mRootView = view.findViewById(R.id.fragment_menu_main);
        applyAnimatedBackground(LauncherPreferences.PREF_ANIMATED_THEME);
        applyAnimatedButtons(LauncherPreferences.PREF_ANIMATED_THEME);

        // Listen for live preference changes
        mAnimatedThemePrefListener = (prefs, key) -> {
            if ("animated_theme".equals(key)) {
                boolean enabled = prefs.getBoolean("animated_theme", true);
                applyAnimatedBackground(enabled);
                applyAnimatedButtons(enabled);
            }
        };
    }

    private void applyAnimatedBackground(boolean enabled) {
        if (mRootView == null) return;
        if (enabled) {
            mRootView.setBackgroundResource(R.drawable.animated_launcher_background);
            Drawable background = mRootView.getBackground();
            if (background instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) background;
                animationDrawable.setEnterFadeDuration(1000);
                animationDrawable.setExitFadeDuration(1500);
                animationDrawable.start();
            }
        } else {
            Drawable background = mRootView.getBackground();
            if (background instanceof AnimationDrawable) {
                ((AnimationDrawable) background).stop();
            }
            mRootView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background_app));
        }
    }

    private void applyAnimatedButtons(boolean enabled) {
        if (mHomeButtons == null) return;
        if (enabled) {
            for (View v : mHomeButtons) {
                if (v == null) continue;
                v.setBackgroundResource(R.drawable.animated_button_background);
                Drawable bg = v.getBackground();
                if (bg instanceof StateListDrawable) {
                    Drawable current = bg.getCurrent();
                    if (current instanceof AnimationDrawable) {
                        AnimationDrawable ad = (AnimationDrawable) current;
                        ad.setEnterFadeDuration(500);
                        ad.setExitFadeDuration(800);
                        ad.start();
                    }
                } else if (bg instanceof AnimationDrawable) {
                    AnimationDrawable ad = (AnimationDrawable) bg;
                    ad.setEnterFadeDuration(500);
                    ad.setExitFadeDuration(800);
                    ad.start();
                }
            }
        } else {
            // Restore themed ripple background
            for (View v : mHomeButtons) {
                if (v == null) continue;
                Drawable bg = v.getBackground();
                if (bg instanceof AnimationDrawable) {
                    ((AnimationDrawable) bg).stop();
                } else if (bg instanceof StateListDrawable) {
                    Drawable current = ((StateListDrawable) bg).getCurrent();
                    if (current instanceof AnimationDrawable) {
                        ((AnimationDrawable) current).stop();
                    }
                }
                v.setBackgroundResource(R.drawable.ripple_menu_button);
            }
        }
    }

    private File getCurrentProfileDirectory() {
        return InstanceManager.getSelectedListedInstance().getGameDirectory();
    }

    @Override
    public void onResume() {
        super.onResume();
        ExtraCore.setValue(ExtraConstants.REFRESH_ACCOUNT_SPINNER, true);
        // Ensure current state is applied and listen for changes
        if (LauncherPreferences.DEFAULT_PREF != null) {
            LauncherPreferences.DEFAULT_PREF.registerOnSharedPreferenceChangeListener(mAnimatedThemePrefListener);
            boolean enabled = LauncherPreferences.DEFAULT_PREF.getBoolean("animated_theme", true);
            applyAnimatedBackground(enabled);
            applyAnimatedButtons(enabled);
        }
    }

    @Override
    public void onPause() {
        if (LauncherPreferences.DEFAULT_PREF != null && mAnimatedThemePrefListener != null) {
            LauncherPreferences.DEFAULT_PREF.unregisterOnSharedPreferenceChangeListener(mAnimatedThemePrefListener);
        }
        super.onPause();
    }

    private void runInstallerWithConfirmation(boolean isCustomArgs) {
        if (ProgressKeeper.getTaskCount() == 0)
            Tools.installMod(requireActivity(), isCustomArgs);
        else
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
    }
}
