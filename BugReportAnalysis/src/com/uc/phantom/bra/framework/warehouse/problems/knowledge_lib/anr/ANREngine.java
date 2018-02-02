package com.uc.phantom.bra.framework.warehouse.problems.knowledge_lib.anr;

import com.uc.phantom.bra.framework.warehouse.problems.knowledge_lib.BaseProblem;
import com.uc.phantom.bra.ui.swing.utils.Logger;
import com.uc.phantom.bra.ui.swing.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ANR Problem Engine.
 * @Author Tam.
 * Modifications :
 * 2017/4/27 : Created.
 */
public class ANREngine {

    private final static String TAG = "ANREngine";

    private List<OnDiagnoseCompelteListener> mOnDiagnoseCompelteListenerList;

    private static ANREngine sANREngine;
    public synchronized static ANREngine getInstance() {
        if (sANREngine == null) {
            sANREngine = new ANREngine();
        }

        return sANREngine;
    }

    public ANREngine() {
        mOnDiagnoseCompelteListenerList = new ArrayList<OnDiagnoseCompelteListener>();
    }

    public void recognize(final List<String> anrLogcat, List<String> anrTraces) {
        if (anrLogcat == null) {
            Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_ERROR, "Information not enough, absent of applocat.");
            return;
        }

        List<String> problemStringList = new ArrayList<String>();

        boolean anrStartFlag = false, anrEndFlag = true;
        int totalLen = anrLogcat.size();
        int charIndex;
        String line, tag = "";
        int anrLogcatStartLine = -1;
        for (int i = 0 ; i < totalLen ; i++) {
            line = anrLogcat.get(i);
            if ((charIndex = StringUtils.kmp_indexOf(line, " ANR in")) < 0) {
                if (anrStartFlag && !anrEndFlag) {
                    if (line.contains(tag)) {
                        problemStringList.add(line);
                    } else {
                        anrStartFlag = false;
                        anrEndFlag = true;

                        ConcurrentModifyHashMapANR cmhAnr = new ConcurrentModifyHashMapANR(null);
                        ANRBase.ANRParsedResult anrParsedResult = cmhAnr.recognize(problemStringList, anrTraces);
                        ANRBase.ANRDiagnoseResult diagnoseResult = cmhAnr.diagnose(anrParsedResult);


                        if (diagnoseResult != null) {
                            anrParsedResult.relocate(anrLogcatStartLine,-1);

                            final List<OnDiagnoseCompelteListener> listeners = mOnDiagnoseCompelteListenerList;
                            for (OnDiagnoseCompelteListener listener : listeners) {
                                listener.onDiagnoseComplete(diagnoseResult);
                            }
                        }
                    }
                } else {
                    anrStartFlag = false;
                    anrEndFlag = true;
                    tag = "";
                }
            } else {
                anrStartFlag = true;
                anrEndFlag = false;

                anrLogcatStartLine = i;
                // Search previous until come across a space char.
                int tagStartIndex;
                for (tagStartIndex = charIndex - 1 ; tagStartIndex >= 0 && line.charAt(tagStartIndex) != ' '; tagStartIndex--);

                tagStartIndex++;
                tag = line.substring(tagStartIndex, charIndex);

                problemStringList.add(line);
            }
        }
    }

    public void addOnDiagnoseCompelteListener(OnDiagnoseCompelteListener l) {
        mOnDiagnoseCompelteListenerList.add(l);
    }

    public void removeOnDiagnoseCompelteListener(OnDiagnoseCompelteListener l) {
        mOnDiagnoseCompelteListenerList.remove(l);
    }

    public interface OnDiagnoseCompelteListener {
        void onDiagnoseComplete(ANRBase.ANRDiagnoseResult listener);
    }
}
