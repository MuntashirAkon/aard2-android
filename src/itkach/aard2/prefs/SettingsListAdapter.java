package itkach.aard2.prefs;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.webkit.WebViewFeature;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textview.MaterialTextView;

import java.util.Collections;
import java.util.List;

import itkach.aard2.R;

public class SettingsListAdapter extends BaseAdapter implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final static String TAG = SettingsListAdapter.class.getSimpleName();
    private final Activity context;
    @SuppressWarnings("FieldCanBeLocal")
    private final SharedPreferences userStylePrefs;
    private final View.OnClickListener onDeleteUserStyle;
    private final Fragment fragment;

    final static int POS_UI_THEME = 0;
    final static int POS_FORCE_DARK = 1;
    final static int POS_REMOTE_CONTENT = 2;
    final static int POS_FAV_RANDOM = 3;
    final static int POS_USE_VOLUME_FOR_NAV = 4;
    final static int POS_AUTO_PASTE = 5;
    final static int POS_DISABLE_JS = 6;
    final static int POS_USER_STYLES = 7;
    final static int POS_CLEAR_CACHE = 8;
    final static int POS_ABOUT = 9;

    SettingsListAdapter(Fragment fragment) {
        this.fragment = fragment;
        this.context = fragment.requireActivity();
        this.userStylePrefs = context.getSharedPreferences("userStyles", Activity.MODE_PRIVATE);
        this.userStylePrefs.registerOnSharedPreferenceChangeListener(this);

        this.onDeleteUserStyle = view -> {
            String name = (String) view.getTag();
            deleteUserStyle(name);
        };
    }

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        switch (i) {
            case POS_UI_THEME:
                return getUIThemeSettingsView(convertView, parent);
            case POS_FORCE_DARK:
                return getForceDarkView(convertView, parent);
            case POS_REMOTE_CONTENT:
                return getRemoteContentSettingsView(convertView, parent);
            case POS_FAV_RANDOM:
                return getFavRandomSwitchView(convertView, parent);
            case POS_USE_VOLUME_FOR_NAV:
                return getUseVolumeForNavView(convertView, parent);
            case POS_AUTO_PASTE:
                return getAutoPasteView(convertView, parent);
            case POS_DISABLE_JS:
                return getDisableJavaScriptView(convertView, parent);
            case POS_USER_STYLES:
                return getUserStylesView(convertView, parent);
            case POS_CLEAR_CACHE:
                return getClearCacheView(convertView, parent);
            case POS_ABOUT:
                return getAboutView(convertView, parent);
        }
        return null;
    }

    private View getUIThemeSettingsView(View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.settings_ui_theme_item, parent, false);
            MaterialCardView cardView = view.findViewById(R.id.card_view);
            cardView.setCardBackgroundColor(SurfaceColors.SURFACE_1.getColor(view.getContext()));

            String currentValue = AppPrefs.getPreferredTheme();

            View.OnClickListener clickListener = view1 -> {
                String value = null;
                int id = view1.getId();
                if (id == R.id.setting_ui_theme_auto) {
                    value = AppPrefs.PREF_UI_THEME_AUTO;
                } else if (id == R.id.setting_ui_theme_light) {
                    value = AppPrefs.PREF_UI_THEME_LIGHT;
                } else if (id == R.id.setting_ui_theme_dark) {
                    value = AppPrefs.PREF_UI_THEME_DARK;
                }
                if (value != null) {
                    AppPrefs.setPreferredTheme(value);
                }
                context.recreate();
            };
            RadioButton btnAuto = view.findViewById(R.id.setting_ui_theme_auto);
            RadioButton btnLight = view.findViewById(R.id.setting_ui_theme_light);
            RadioButton btnDark = view.findViewById(R.id.setting_ui_theme_dark);
            btnAuto.setOnClickListener(clickListener);
            btnLight.setOnClickListener(clickListener);
            btnDark.setOnClickListener(clickListener);
            btnAuto.setChecked(currentValue.equals(AppPrefs.PREF_UI_THEME_AUTO));
            btnLight.setChecked(currentValue.equals(AppPrefs.PREF_UI_THEME_LIGHT));
            btnDark.setChecked(currentValue.equals(AppPrefs.PREF_UI_THEME_DARK));
        }
        return view;
    }

    private View getForceDarkView(View convertView, ViewGroup parent) {
        View view;
        MaterialSwitch toggle;
        if (convertView != null) {
            view = convertView;
            toggle = view.findViewById(R.id.setting_switch);
        } else {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.settings_switch, parent, false);
            MaterialCardView cardView = view.findViewById(R.id.card_view);
            cardView.setCardBackgroundColor(SurfaceColors.SURFACE_1.getColor(view.getContext()));

            toggle = view.findViewById(R.id.setting_switch);
            toggle.setText(R.string.setting_enable_force_dark_web_view);
            toggle.setEnabled(WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING));
            toggle.setOnClickListener(v -> {
                boolean currentValue = ArticleViewPrefs.enableForceDark();
                boolean newValue = !currentValue;
                ArticleViewPrefs.setEnableForceDark(newValue);
                toggle.setChecked(newValue);
            });
            view.findViewById(R.id.setting_subtitle).setVisibility(View.GONE);
        }
        toggle.setChecked(ArticleViewPrefs.enableForceDark());
        return view;
    }

    private View getFavRandomSwitchView(View convertView, ViewGroup parent) {
        View view;
        MaterialSwitch toggle;
        if (convertView != null) {
            view = convertView;
            toggle = view.findViewById(R.id.setting_switch);
        } else {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.settings_switch, parent, false);
            MaterialCardView cardView = view.findViewById(R.id.card_view);
            cardView.setCardBackgroundColor(SurfaceColors.SURFACE_1.getColor(view.getContext()));

            toggle = view.findViewById(R.id.setting_switch);
            toggle.setText(R.string.setting_fav_random_search);
            toggle.setOnClickListener(v -> {
                boolean currentValue = AppPrefs.useOnlyFavoritesForRandomLookups();
                boolean newValue = !currentValue;
                AppPrefs.setUseOnlyFavoritesForRandomLookups(newValue);
                toggle.setChecked(newValue);
            });
            view.findViewById(R.id.setting_subtitle).setVisibility(View.GONE);
        }
        toggle.setChecked(AppPrefs.useOnlyFavoritesForRandomLookups());
        return view;
    }

    private View getUseVolumeForNavView(View convertView, ViewGroup parent) {
        View view;
        MaterialSwitch toggle;
        if (convertView != null) {
            view = convertView;
            toggle = view.findViewById(R.id.setting_switch);
        } else {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.settings_switch, parent, false);
            MaterialCardView cardView = view.findViewById(R.id.card_view);
            cardView.setCardBackgroundColor(SurfaceColors.SURFACE_1.getColor(view.getContext()));

            toggle = view.findViewById(R.id.setting_switch);
            toggle.setText(R.string.setting_use_volume_for_nav);
            toggle.setOnClickListener(v -> {
                boolean currentValue = AppPrefs.useVolumeKeysForNavigation();
                boolean newValue = !currentValue;
                AppPrefs.setUseVolumeKeysForNavigation(newValue);
                toggle.setChecked(newValue);
            });
            view.findViewById(R.id.setting_subtitle).setVisibility(View.GONE);
        }
        toggle.setChecked(AppPrefs.useVolumeKeysForNavigation());
        return view;
    }

    @NonNull
    private View getAutoPasteView(@Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        MaterialSwitch toggle;
        if (convertView != null) {
            view = convertView;
            toggle = view.findViewById(R.id.setting_switch);
        } else {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.settings_switch, parent, false);
            MaterialCardView cardView = view.findViewById(R.id.card_view);
            cardView.setCardBackgroundColor(SurfaceColors.SURFACE_1.getColor(view.getContext()));

            toggle = view.findViewById(R.id.setting_switch);
            toggle.setText(R.string.setting_auto_paste);
            toggle.setOnClickListener(v -> {
                boolean currentValue = AppPrefs.autoPasteInLookup();
                boolean newValue = !currentValue;
                AppPrefs.setAutoPasteInLookup(newValue);
                toggle.setChecked(newValue);
            });
            view.findViewById(R.id.setting_subtitle).setVisibility(View.GONE);
        }
        toggle.setChecked(AppPrefs.autoPasteInLookup());
        return view;
    }

    private View getDisableJavaScriptView(View convertView, ViewGroup parent) {
        View view;
        MaterialSwitch toggle;
        if (convertView != null) {
            view = convertView;
            toggle = view.findViewById(R.id.setting_switch);
        } else {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.settings_switch, parent, false);
            MaterialCardView cardView = view.findViewById(R.id.card_view);
            cardView.setCardBackgroundColor(SurfaceColors.SURFACE_1.getColor(view.getContext()));

            toggle = view.findViewById(R.id.setting_switch);
            toggle.setText(R.string.setting_disable_javascript_title);
            toggle.setOnClickListener(v -> {
                boolean currentValue = ArticleViewPrefs.disableJavaScript();
                boolean newValue = !currentValue;
                ArticleViewPrefs.setDisableJavaScript(newValue);
                toggle.setChecked(newValue);
            });
            MaterialTextView subtitleView = view.findViewById(R.id.setting_subtitle);
            subtitleView.setVisibility(View.VISIBLE);
            subtitleView.setText(R.string.setting_disable_javascript_subtitle);
        }
        toggle.setChecked(ArticleViewPrefs.disableJavaScript());
        return view;
    }


    private View getUserStylesView(View convertView, final ViewGroup parent) {
        View view;
        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView != null) {
            view = convertView;
        } else {
            view = inflater.inflate(R.layout.settings_user_styles_item, parent, false);
            MaterialCardView cardView = view.findViewById(R.id.card_view);
            cardView.setCardBackgroundColor(SurfaceColors.SURFACE_1.getColor(view.getContext()));
            MaterialButton btnAdd = view.findViewById(R.id.setting_btn_add_user_style);
            btnAdd.setOnClickListener(view1 -> {
                try {
                    ((SettingsFragment) fragment).userStylesChooser.launch("text/*");
                } catch (ActivityNotFoundException e) {
                    Log.d(TAG, "Not activity to get content", e);
                    Toast.makeText(context, R.string.msg_no_activity_to_get_content,
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        List<String> userStyleNames = UserStylesPrefs.listStyleNames();
        Collections.sort(userStyleNames);

        View emptyView = view.findViewById(R.id.setting_user_styles_empty);
        emptyView.setVisibility(userStyleNames.size() == 0 ? View.VISIBLE : View.GONE);

        LinearLayoutCompat userStyleListLayout = view.findViewById(R.id.setting_user_styles_list);
        userStyleListLayout.removeAllViews();
        for (String userStyleName : userStyleNames) {
            View styleItemView = inflater.inflate(R.layout.user_styles_list_item, parent,
                    false);
            ImageView btnDelete = styleItemView.findViewById(R.id.user_styles_list_btn_delete);
            btnDelete.setOnClickListener(onDeleteUserStyle);
            btnDelete.setTag(userStyleName);

            TextView nameView = styleItemView.findViewById(R.id.user_styles_list_name);
            nameView.setText(userStyleName);

            userStyleListLayout.addView(styleItemView);
        }

        return view;
    }

    private void deleteUserStyle(final String name) {
        String message = context.getString(R.string.setting_user_style_confirm_forget, name);
        new MaterialAlertDialogBuilder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.setting_user_styles)
                .setMessage(message)
                .setPositiveButton(R.string.action_yes, (dialog, which) -> {
                    Log.d(TAG, "Deleting user style " + name);
                    UserStylesPrefs.removeStyle(name);
                })
                .setNegativeButton(R.string.action_no, null)
                .show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        notifyDataSetChanged();
    }

    private View getRemoteContentSettingsView(View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.settings_remote_content_item, parent, false);
            MaterialCardView cardView = view.findViewById(R.id.card_view);
            cardView.setCardBackgroundColor(SurfaceColors.SURFACE_1.getColor(view.getContext()));

            String currentValue = ArticleViewPrefs.getRemoteContentPreference();
            View.OnClickListener clickListener = view1 -> {
                String value = null;
                int id = view1.getId();
                if (id == R.id.setting_remote_content_always) {
                    value = ArticleViewPrefs.PREF_REMOTE_CONTENT_ALWAYS;
                } else if (id == R.id.setting_remote_content_wifi) {
                    value = ArticleViewPrefs.PREF_REMOTE_CONTENT_WIFI;
                } else if (id == R.id.setting_remote_content_never) {
                    value = ArticleViewPrefs.PREF_REMOTE_CONTENT_NEVER;
                }
                if (value != null) {
                    ArticleViewPrefs.setRemoteContentPreference(value);
                }
            };
            RadioButton btnAlways = view.findViewById(R.id.setting_remote_content_always);
            RadioButton btnWiFi = view.findViewById(R.id.setting_remote_content_wifi);
            RadioButton btnNever = view.findViewById(R.id.setting_remote_content_never);
            btnAlways.setOnClickListener(clickListener);
            btnWiFi.setOnClickListener(clickListener);
            btnNever.setOnClickListener(clickListener);
            btnAlways.setChecked(currentValue.equals(ArticleViewPrefs.PREF_REMOTE_CONTENT_ALWAYS));
            btnWiFi.setChecked(currentValue.equals(ArticleViewPrefs.PREF_REMOTE_CONTENT_WIFI));
            btnNever.setChecked(currentValue.equals(ArticleViewPrefs.PREF_REMOTE_CONTENT_NEVER));
        }
        return view;
    }

    private View getClearCacheView(View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            final Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.settings_clear_cache_item, parent, false);
            MaterialCardView cardView = view.findViewById(R.id.card_view);
            cardView.setCardBackgroundColor(SurfaceColors.SURFACE_1.getColor(view.getContext()));
            cardView.setOnClickListener(v -> new MaterialAlertDialogBuilder(fragment.requireActivity())
                    .setMessage(R.string.confirm_clear_cached_content)
                    .setPositiveButton(R.string.action_yes, (dialog, id1) -> {
                        WebView webView = new WebView(fragment.requireActivity());
                        webView.clearCache(true);
                    })
                    .setNegativeButton(R.string.action_no, null)
                    .show());
        }
        return view;
    }

    private View getAboutView(View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            final Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.settings_about_item, parent, false);
            MaterialCardView cardView = view.findViewById(R.id.card_view);
            cardView.setCardBackgroundColor(SurfaceColors.SURFACE_1.getColor(view.getContext()));

            String appName = context.getString(R.string.app_name);
            String title = context.getString(R.string.setting_about, appName);

            TextView titleView = view.findViewById(R.id.setting_about);
            titleView.setText(title);

            String licenseName = context.getString(R.string.application_license_name);
            final String licenseUrl = context.getString(R.string.application_license_url);
            String license = context.getString(R.string.application_license, licenseUrl, licenseName);
            TextView licenseView = view.findViewById(R.id.application_license);
            licenseView.setOnClickListener(view1 -> {
                Uri uri = Uri.parse(licenseUrl);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(browserIntent);
            });
            licenseView.setText(HtmlCompat.fromHtml(license.trim(), HtmlCompat.FROM_HTML_MODE_LEGACY));

            PackageManager manager = context.getPackageManager();
            String versionName;
            try {
                PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
                versionName = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                versionName = "?";
            }

            TextView versionView = view.findViewById(R.id.application_version);
            versionView.setText(context.getString(R.string.application_version, versionName));

        }
        return view;
    }

}