package top.jie65535.j24

import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.shuntingyard.ShuntingYard
import net.objecthunter.exp4j.tokenizer.NumberToken
import net.objecthunter.exp4j.tokenizer.Token
import kotlin.random.Random

class Point24 {
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

        val tokens = ShuntingYard.convertToRPN(
            expr,
            null,
            mapOf(),
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
                    throw IllegalArgumentException("不能使用未允许数值")
            }
        }
        if (nums.isNotEmpty())
            throw IllegalArgumentException("必须使用所有数值")

        return ExpressionBuilder(expr)
            .implicitMultiplication(false)
            .build()
            .evaluate()
    }
}