/*
 * Copyright (C) 2026 yztz
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package top.yztz.msggo.adapters;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import top.yztz.msggo.R;
import top.yztz.msggo.data.Message;
import top.yztz.msggo.data.MessageState;

public class SendingListAdapter extends RecyclerView.Adapter<SendingListAdapter.ViewHolder> {

    private final List<Message> messages = new ArrayList<>();

    public SendingListAdapter(Context context) {
    }

    public void setMessages(List<Message> newMessages) {
        this.messages.clear();
        if (newMessages != null) {
            this.messages.addAll(newMessages);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sending_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        Message message = messages.get(position);

        holder.tvPhone.setText(message.getPhone());
        holder.tvContent.setText(message.getContent().replace('\n', ' '));
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            MessageState newState = (MessageState) payloads.get(0);
            holder.transitionToState(newState, true); // true = 使用动画
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);

        holder.tvPhone.setText(message.getPhone());
        holder.tvContent.setText(message.getContent().replace('\n', ' '));
        holder.transitionToState(message.getState(), false); // false = 不使用动画
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvPhone, tvContent, tvStatus;
        ImageView ivStatusIcon;
        LinearProgressIndicator progressIndicator;

        private AnimatorSet currentAnimator = null;

        private final java.util.EnumMap<MessageState, StateStyle> styleMap = new java.util.EnumMap<>(MessageState.class);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvStatus = itemView.findViewById(R.id.tv_status);
            ivStatusIcon = itemView.findViewById(R.id.iv_status_icon);
            progressIndicator = itemView.findViewById(R.id.progress_indicator);

            int bg       = MaterialColors.getColor(itemView, R.attr.colorSurface);
            int onBg     = MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurface);
            int bgOk     = MaterialColors.getColor(itemView, R.attr.colorPrimaryContainer);
            int onBgOk   = MaterialColors.getColor(itemView, R.attr.colorOnPrimaryContainer);
            int bgErr    = MaterialColors.getColor(itemView, R.attr.colorErrorContainer);
            int onBgErr  = MaterialColors.getColor(itemView, R.attr.colorOnErrorContainer);

            styleMap.put(MessageState.PENDING,   new StateStyle(bg,    onBg,    R.drawable.ic_hourglass, R.string.pending,   1f,   false));
            styleMap.put(MessageState.WAITING,   new StateStyle(bg,    onBg,    R.drawable.ic_hourglass, R.string.waiting,   1f,   true));
            styleMap.put(MessageState.SUBMITTED, new StateStyle(bg,    onBg,    R.drawable.ic_hourglass, R.string.submitted, 1f,   true));
            styleMap.put(MessageState.PAUSED,    new StateStyle(bg,    onBg,    R.drawable.ic_pause,     R.string.paused,    0.6f, false));
            styleMap.put(MessageState.SENT,      new StateStyle(bgOk,  onBgOk,  R.drawable.ic_success,   R.string.sent,      1f,   false));
            styleMap.put(MessageState.FAILED,    new StateStyle(bgErr, onBgErr, R.drawable.ic_error,     R.string.failed,    1f,   false));
        }

        private StateStyle getStyleForState(MessageState state) {
            return styleMap.get(state);
        }

        /**
         * 统一的状态转换方法
         * @param newState 新状态
         * @param animate 是否使用动画
         */
        public void transitionToState(MessageState newState, boolean animate) {
            cancelAnimation();

            StateStyle style = getStyleForState(newState);

            // 更新文本和进度条
            tvStatus.setText(style.textRes);
            progressIndicator.setVisibility(style.showProgress ? View.VISIBLE : View.GONE);

            if (!animate) {
                // 直接设置，无动画
                applyStyleImmediately(style);
            } else {
                // 使用动画过渡
                animateToStyle(style);
            }
        }

        private void applyStyleImmediately(StateStyle style) {
            cardView.setCardBackgroundColor(style.bgColor);
            tvStatus.setTextColor(style.fgColor);
            ivStatusIcon.setImageResource(style.iconRes);
            ivStatusIcon.setColorFilter(style.fgColor);
            cardView.setAlpha(style.alpha);
            ivStatusIcon.setAlpha(1f);
        }

        private void animateToStyle(StateStyle style) {
            List<Animator> animators = new ArrayList<>();

            // 背景色动画
            ObjectAnimator bgAnim = ObjectAnimator.ofObject(
                    cardView,
                    "cardBackgroundColor",
                    new ArgbEvaluator(),
                    cardView.getCardBackgroundColor().getDefaultColor(),
                    style.bgColor
            );
            animators.add(bgAnim);

            // 文字颜色动画
            ObjectAnimator textAnim = ObjectAnimator.ofObject(
                    tvStatus,
                    "textColor",
                    new ArgbEvaluator(),
                    tvStatus.getCurrentTextColor(),
                    style.fgColor
            );
            animators.add(textAnim);

            // 透明度动画
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(
                    cardView,
                    "alpha",
                    cardView.getAlpha(),
                    style.alpha
            );
            animators.add(alphaAnim);

            // 图标切换动画（淡出 -> 切换 -> 淡入）
            ObjectAnimator iconFadeOut = ObjectAnimator.ofFloat(ivStatusIcon, "alpha", 1f, 0f);
            iconFadeOut.setDuration(150);

            ObjectAnimator iconFadeIn = ObjectAnimator.ofFloat(ivStatusIcon, "alpha", 0f, 1f);
            iconFadeIn.setDuration(150);
            iconFadeIn.setStartDelay(150);
            iconFadeIn.addListener(new android.animation.AnimatorListenerAdapter() {
                private boolean isCancelled = false;

                @Override
                public void onAnimationStart(android.animation.Animator animation) {
                    // 在淡入开始时切换图标
                    ivStatusIcon.setImageResource(style.iconRes);
                    ivStatusIcon.setColorFilter(style.fgColor);
                }

                @Override
                public void onAnimationCancel(android.animation.Animator animation) {
                    isCancelled = true;
                }

                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    if (!isCancelled) {
                        ivStatusIcon.setAlpha(1f);
                    }
                }
            });

            animators.add(iconFadeOut);
            animators.add(iconFadeIn);

            // 组合所有动画
            currentAnimator = new AnimatorSet();
            currentAnimator.playTogether(animators);
            currentAnimator.setDuration(300);
            currentAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    currentAnimator = null;
                }

                @Override
                public void onAnimationCancel(android.animation.Animator animation) {
                    currentAnimator = null;
                }
            });
            currentAnimator.start();
        }

        private void cancelAnimation() {
            if (currentAnimator != null) {
                currentAnimator.cancel();
                currentAnimator = null;
            }
        }

        static class StateStyle {
            final int bgColor, fgColor, iconRes, textRes;
            final float alpha;
            final boolean showProgress;

            StateStyle(int bgColor, int fgColor, int iconRes, int textRes, float alpha, boolean showProgress) {
                this.bgColor = bgColor;
                this.fgColor = fgColor;
                this.iconRes = iconRes;
                this.textRes = textRes;
                this.alpha = alpha;
                this.showProgress = showProgress;
            }
        }
    }
}