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
package top.yztz.msggo.activities;

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
            SendingActivity.MessageState newState = (SendingActivity.MessageState) payloads.get(0);
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

        // 缓存颜色值，避免重复获取
        private final int colorDefault;
        private final int colorOnDefault;
        private final int colorSuccess;
        private final int colorOnSuccess;
        private final int colorError;
        private final int colorOnError;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvStatus = itemView.findViewById(R.id.tv_status);
            ivStatusIcon = itemView.findViewById(R.id.iv_status_icon);
            progressIndicator = itemView.findViewById(R.id.progress_indicator);

            // 初始化时获取一次颜色值
            colorDefault = MaterialColors.getColor(itemView, R.attr.colorSurface);
            colorError = MaterialColors.getColor(itemView, R.attr.colorErrorContainer);
            colorSuccess = MaterialColors.getColor(itemView, R.attr.colorPrimaryContainer);
            colorOnDefault = MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurface);
            colorOnError = MaterialColors.getColor(itemView, R.attr.colorOnErrorContainer);
            colorOnSuccess = MaterialColors.getColor(itemView, R.attr.colorOnPrimaryContainer);
        }

        private StateStyle getStyleForState(SendingActivity.MessageState state) {
            StateStyle style = new StateStyle();

            // 默认样式
            style.bgColor = colorDefault;
            style.textColor = colorOnDefault;
            style.iconColor = colorOnDefault;
            style.iconRes = R.drawable.ic_hourglass;
            style.alpha = 1f;
            style.showProgress = false;

            switch (state) {
                case PENDING:
                    style.textRes = R.string.pending;
                    break;
                case WAITING:
                    style.textRes = R.string.waiting;
                    style.showProgress = true;
                    break;
                case SUBMITTED:
                    style.textRes = R.string.submitted;
                    style.showProgress = true;
                    break;
                case PAUSED:
                    style.textRes = R.string.paused;
                    style.iconRes = R.drawable.ic_pause;
                    style.alpha = 0.6f;
                    break;
                case SENT:
                    style.textRes = R.string.sent;
                    style.bgColor = colorSuccess;
                    style.textColor = colorOnSuccess;
                    style.iconColor = colorOnSuccess;
                    style.iconRes = R.drawable.ic_success;
                    break;
                case FAILED:
                    style.textRes = R.string.failed;
                    style.bgColor = colorError;
                    style.textColor = colorOnError;
                    style.iconColor = colorOnError;
                    style.iconRes = R.drawable.ic_error;
                    break;
            }
            return style;
        }

        /**
         * 统一的状态转换方法
         * @param newState 新状态
         * @param animate 是否使用动画
         */
        public void transitionToState(SendingActivity.MessageState newState, boolean animate) {
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
            tvStatus.setTextColor(style.textColor);
            ivStatusIcon.setImageResource(style.iconRes);
            ivStatusIcon.setColorFilter(style.iconColor);
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
                    style.textColor
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
                    ivStatusIcon.setColorFilter(style.iconColor);
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
            int bgColor, textColor, iconColor, iconRes, textRes;
            float alpha;
            boolean showProgress;
        }
    }
}