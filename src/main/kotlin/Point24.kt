package top.jie65535.j24

import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.operator.Operator
import net.objecthunter.exp4j.shuntingyard.ShuntingYard
import net.objecthunter.exp4j.tokenizer.NumberToken
import net.objecthunter.exp4j.tokenizer.Token
import java.time.LocalDateTime
import kotlin.random.Random

class Point24 {
    companion object {
        val myOperators = listOf(
            object : Operator(">>", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
                override fun apply(vararg args: Double): Double {
                    return (args[0].toInt() shr args[1].toInt()).toDouble()
                }
            },
            object : Operator("<<", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
                override fun apply(vararg args: Double): Double {
                    return (args[0].toInt() shl args[1].toInt()).toDouble()
                }
            },
            object : Operator("&", 2, true, Operator.PRECEDENCE_ADDITION - 2) {
                override fun apply(vararg args: Double): Double {
                    return (args[0].toInt() and args[1].toInt()).toDouble()
                }
            },
            object : Operator("^", 2, true, Operator.PRECEDENCE_ADDITION - 3) {
                override fun apply(vararg args: Double): Double {
                    return (args[0].toInt() xor args[1].toInt()).toDouble()
                }
            },
            object : Operator("|", 2, true, Operator.PRECEDENCE_ADDITION - 4) {
                override fun apply(vararg args: Double): Double {
                    return (args[0].toInt() or args[1].toInt()).toDouble()
                }
            },
            // 阶乘禁用，因为这会让游戏从24点变成4点
//            object : Operator("!", 1, true, Operator.PRECEDENCE_POWER) {
//                override fun apply(vararg args: Double): Double {
//                    var sum = 1
//                    for (i in 2..args[0].toInt())
//                        sum *= i
//                    return sum.toDouble()
//                }
//            },
        )
        private val myOperatorMap: Map<String, Operator>
        init {
            val m = mutableMapOf<String, Operator>()
            for (opt in myOperators)
                m[opt.symbol] = opt
            myOperatorMap = m
        }
    }

    var points = genPoints()
    var time: LocalDateTime = LocalDateTime.now()

    private fun genPoints() = arrayOf(
        Random.nextInt(1, 14),
        Random.nextInt(1, 14),
        Random.nextInt(1, 14),
        Random.nextInt(1, 14)
    )

    override fun toString() = "[${points[0]}] [${points[1]}] [${points[2]}] [${points[3]}]"

    fun evaluate(expression: String): Double {
        val expr = expression
            .replace('（', '(')
            .replace('）', ')')
            .replace('x', '*')
            .replace('×', '*')
            .replace('÷', '/')
            .replace('－', '-')
            .replace('＋', '+')
            .replace('！', '!')
            .replace('＜', '<')
            .replace('＞', '>')


        if (expr.contains('%'))
            throw IllegalArgumentException("禁止使用%运算符")

        val tokens = ShuntingYard.convertToRPN(
            expr,
            null,
            myOperatorMap,
            null,
            false
        )

        var usedAll = true
        val nums = points.toMutableList()
        for (token in tokens) {
            if (token.type == Token.TOKEN_NUMBER.toInt()) {
                val value = (token as NumberToken).value
                var i = 0
                while (i < nums.size)
                    if (nums[i].toDouble() == value)
                        break
                    else ++i
                if (i < nums.size)
                    nums.removeAt(i)
                else
                    usedAll = false
//                throw IllegalArgumentException("不能使用未得到的数值")
            } else if (token.type == Token.TOKEN_FUNCTION.toInt()) {
                throw IllegalArgumentException("禁止使用函数哦")
            }
        }
        if (nums.isNotEmpty())
            usedAll = false
//            throw IllegalArgumentException("必须使用所有数值")

        val result = ExpressionBuilder(expr)
            .operator(myOperators)
            .implicitMultiplication(false)
            .build()
            .evaluate()
        if (usedAll)
            return result
        else
            throw IllegalArgumentException("结果为$result，请使用系统生成的数值！")
    }
}