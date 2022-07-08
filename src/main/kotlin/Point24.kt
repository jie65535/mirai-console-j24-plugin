package top.jie65535.j24

import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.operator.Operator
import net.objecthunter.exp4j.shuntingyard.ShuntingYard
import net.objecthunter.exp4j.tokenizer.NumberToken
import net.objecthunter.exp4j.tokenizer.Token
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

    fun regenPoints() {
        points = genPoints()
    }

    private fun genPoints() = arrayOf(
        Random.nextInt(1, 14),
        Random.nextInt(1, 14),
        Random.nextInt(1, 14),
        Random.nextInt(1, 14)
    )

    fun evaluate(expression: String): Double {
        val expr = expression.replace('（', '(').replace('）', ')')

        if (expr.contains('%'))
            throw IllegalArgumentException("禁止使用%运算符")

        val tokens = ShuntingYard.convertToRPN(
            expr,
            null,
            myOperatorMap,
            null,
            false
        )

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
                    throw IllegalArgumentException("不能使用未得到的数值")
            } else if (token.type == Token.TOKEN_FUNCTION.toInt()) {
                throw IllegalArgumentException("禁止使用函数哦")
            }
        }
        if (nums.isNotEmpty())
            throw IllegalArgumentException("必须使用所有数值")

        return ExpressionBuilder(expr)
            .operator(myOperators)
            .implicitMultiplication(false)
            .build()
            .evaluate()
    }
}