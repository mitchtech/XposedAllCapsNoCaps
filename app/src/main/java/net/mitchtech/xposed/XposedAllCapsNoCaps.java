
package net.mitchtech.xposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedAllCapsNoCaps implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static final String TAG = XposedAllCapsNoCaps.class.getSimpleName();
    private static final String PKG_NAME = "net.mitchtech.xposed.allcapsnocaps";

    private XSharedPreferences prefs;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        loadPrefs();
    }

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

        // don't proceed if current app is the configuration app
        if (lpparam.packageName.equals(PKG_NAME)) {
            return;
        }
        
        // don't proceed if neither all caps or no caps mode is enabled
        if (!isEnabled("prefAllCaps") && !isEnabled("prefNoCaps")) {
            return;
        }

        // don't proceed if current package is system ui and is disabled
        if (lpparam.packageName.equals("com.android.systemui")) {
            if (!isEnabled("prefSystemUi")) {
                return;
            }
        // don't proceed for other apps if preference is disabled
        } else {
            if (!isEnabled("prefAllApps")) {
                return;
            }
        }

        XC_MethodHook textMethodHook = new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                CharSequence actualText = (CharSequence) methodHookParam.args[0];

                if (actualText != null) {
                    if (isEnabled("prefAllCaps")) {
                        String capsText = actualText.toString().toUpperCase();
                        methodHookParam.args[0] = capsText;
                    } else if (isEnabled("prefNoCaps")) {
                        String capsText = actualText.toString().toLowerCase();
                        methodHookParam.args[0] = capsText;
                    }
                }
            }
        };

        // hook standard text views
        if (isEnabled("prefTextView")) {
            findAndHookMethod(TextView.class, "setText", CharSequence.class,
                    TextView.BufferType.class, boolean.class, int.class, textMethodHook);
            findAndHookMethod(TextView.class, "setHint", CharSequence.class, textMethodHook);
            findAndHookMethod(TextView.class, "append", CharSequence.class, textMethodHook);
            findAndHookMethod(TextView.class, "append", CharSequence.class, int.class, int.class,
                    textMethodHook);
        }

        // hook GL canvas text views
        if (isEnabled("prefGlText")) {
            findAndHookMethod("android.view.GLES20Canvas", null, "drawText", String.class,
                    float.class, float.class, Paint.class, textMethodHook);
        }

        // hook editable text views
        if (isEnabled("prefEditText")) {
            findAndHookMethod(EditText.class, "setText", CharSequence.class,
                    TextView.BufferType.class, textMethodHook);

            findAndHookConstructor(EditText.class, Context.class, new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    final EditText editText = (EditText) methodHookParam.thisObject;
                    editText.setAllCaps(true);
                    editText.setOnFocusChangeListener(new OnFocusChangeListener() {

                        @Override
                        public void onFocusChange(View view, boolean hasFocus) {
                            if (!hasFocus) {
                                editText.setAllCaps(true);
                            }
                        }
                    });
                }
            });
        }
    }

    private boolean isEnabled(String prefName) {
        prefs.reload();
        return prefs.getBoolean(prefName, false);
    }

    private void loadPrefs() {
        prefs = new XSharedPreferences(PKG_NAME);
        prefs.makeWorldReadable();
        XposedBridge.log(TAG + ": prefs loaded.");
    }

}
