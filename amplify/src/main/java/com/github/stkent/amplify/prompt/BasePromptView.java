package com.github.stkent.amplify.prompt;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.stkent.amplify.prompt.interfaces.IPromptPresenter;
import com.github.stkent.amplify.prompt.interfaces.IPromptView;
import com.github.stkent.amplify.prompt.interfaces.IQuestion;
import com.github.stkent.amplify.prompt.interfaces.IQuestionPresenter;
import com.github.stkent.amplify.prompt.interfaces.IQuestionView;
import com.github.stkent.amplify.prompt.interfaces.IThanksView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.github.stkent.amplify.prompt.interfaces.IPromptPresenter.UserOpinion.NEGATIVE;
import static com.github.stkent.amplify.prompt.interfaces.IPromptPresenter.UserOpinion.POSITIVE;

abstract class BasePromptView extends FrameLayout implements IPromptView {

    @NonNull
    protected abstract <T extends View & IQuestionView> T getUserOpinionQuestionView();

    @NonNull
    protected abstract <T extends View & IQuestionView> T getPositiveFeedbackQuestionView();

    @NonNull
    protected abstract <T extends View & IQuestionView> T getCriticalFeedbackQuestionView();

    @NonNull
    protected abstract <T extends View & IThanksView>   T getThanksView();

    protected boolean isDisplayed = false;

    private final IQuestionPresenter userOpinionQuestionPresenter =
            new IQuestionPresenter() {
                @Override
                public void userRespondedPositively() {
                    promptPresenter.setUserOpinion(POSITIVE);
                }

                @Override
                public void userRespondedNegatively() {
                    promptPresenter.setUserOpinion(NEGATIVE);
                }
            };

    private final IQuestionPresenter feedbackQuestionPresenter =
            new IQuestionPresenter() {
                @Override
                public void userRespondedPositively() {
                    promptPresenter.userAgreedToGiveFeedback();
                }

                @Override
                public void userRespondedNegatively() {
                    promptPresenter.userDeclinedToGiveFeedback();
                }
            };

    private IPromptPresenter promptPresenter;
    private BasePromptViewConfig basePromptViewConfig;

    public BasePromptView(final Context context) {
        this(context, null);
    }

    public BasePromptView(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BasePromptView(
            final Context context,
            @Nullable final AttributeSet attributeSet,
            final int defStyleAttr) {

        super(context, attributeSet, defStyleAttr);
        setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        final TypedArray typedArray = getContext().getTheme()
                .obtainStyledAttributes(attributeSet, R.styleable.BasePromptView, 0, 0);

        loadConfigFromTypedArray(typedArray);

        typedArray.recycle();
    }

    @Override
    public final void setPresenter(@NonNull final IPromptPresenter promptPresenter) {
        this.promptPresenter = promptPresenter;
    }

    @Override
    public final void queryUserOpinion() {
        final IQuestionView userOpinionQuestionView = getUserOpinionQuestionView();
        userOpinionQuestionView.bind(basePromptViewConfig.getUserOpinionQuestion());
        userOpinionQuestionView.setPresenter(userOpinionQuestionPresenter);

        setContentView((View) userOpinionQuestionView);

        isDisplayed = true;
    }

    @Override
    public final void requestPositiveFeedback() {
        final IQuestionView positiveFeedbackQuestionView = getPositiveFeedbackQuestionView();
        positiveFeedbackQuestionView.bind(basePromptViewConfig.getPositiveFeedbackQuestion());
        positiveFeedbackQuestionView.setPresenter(feedbackQuestionPresenter);

        setContentView((View) positiveFeedbackQuestionView);
    }

    @Override
    public final void requestCriticalFeedback() {
        final IQuestionView criticalFeedbackQuestionView = getCriticalFeedbackQuestionView();
        criticalFeedbackQuestionView.bind(basePromptViewConfig.getCriticalFeedbackQuestion());
        criticalFeedbackQuestionView.setPresenter(feedbackQuestionPresenter);

        setContentView((View) criticalFeedbackQuestionView);
    }

    @Override
    public final void thankUser() {
        final IThanksView thanksView = getThanksView();
        thanksView.bind(basePromptViewConfig.getThanks());

        setContentView((View) thanksView);

        promptPresenter = null;
    }

    @Override
    public final void dismiss() {
        setVisibility(GONE);
        promptPresenter = null;
    }

    private void loadConfigFromTypedArray(final TypedArray typedArray) {
        basePromptViewConfig = new BasePromptViewConfig(typedArray);
    }

    private void setContentView(@NonNull final View view) {
        removeAllViews();
        addView(view, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    }

}
