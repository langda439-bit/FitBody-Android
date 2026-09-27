package com.fitbody.app.feature.training

import com.fitbody.app.data.db.GOAL_GAIN
import com.fitbody.app.data.db.GOAL_LOSE
import com.fitbody.app.data.db.PlanEntity
import org.json.JSONArray

/**
 * 个性化训练计划生成器（骨架）。
 *
 * 正式版结合体测数据、目标、可训练天数与场地，生成包含动作/组数/次数的周计划；
 * 当前生成按目标区分的训练主题周安排（JSON），动作明细在后续迭代补充。
 */
class TrainingPlanGenerator {

    fun generate(
        userId: Long,
        goalType: String,
        daysPerWeek: Int
    ): PlanEntity {
        val themes = when (goalType) {
            GOAL_LOSE -> listOf("力量-全身", "有氧", "力量-上肢+核心", "有氧", "力量-下肢", "有氧/休息", "休息")
            GOAL_GAIN -> listOf("胸+三头", "背+二头", "休息", "腿", "肩+核心", "弱项补强", "休息")
            else -> listOf("力量-全身", "有氧", "休息", "力量-全身", "有氧", "休息", "休息")
        }

        val week = JSONArray()
        for (day in 0 until 7) {
            val scheduled = day < daysPerWeek
            week.put(
                JSONArray()
                    .put(day)
                    .put(if (scheduled) themes[day] else "休息")
                    .put(if (scheduled) 45 else 0)
            )
        }

        return PlanEntity(
            userId = userId,
            goalType = goalType,
            weeks = 8,
            daysPerWeek = daysPerWeek,
            scheduleJson = week.toString(),
            active = true
        )
    }
}
