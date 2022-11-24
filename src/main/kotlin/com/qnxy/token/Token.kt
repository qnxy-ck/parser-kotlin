package com.qnxy.token

import com.fasterxml.jackson.annotation.JsonValue

/**
 *
 * @author ck
 * 2022/11/24
 */
interface Token<T> {
    val data: T?
        get() = null
}

@JvmInline
value class NumberToken(override val data: Int) : Token<Int>

@JvmInline
value class StringToken(override val data: String) : Token<String>

@JvmInline
value class IdentifierToken(override val data: String) : Token<String>

object SemicolonToken : Token<Nothing>
object CurlyBracketLToken : Token<Nothing>
object CurlyBracketRToken : Token<Nothing>
object OpenParenthesisToken : Token<Nothing>
object CloseParenthesisToken : Token<Nothing>
object CommaToken : Token<Nothing>
object LetToken : Token<Nothing>
object IfToken : Token<Nothing>
object ElseToken : Token<Nothing>
object NullToken : Token<Nothing>
object WhileToken : Token<Nothing>
object DoToken : Token<Nothing>
object ForToken : Token<Nothing>
object DefToken : Token<Nothing>
object ReturnToken : Token<Nothing>
object DotToken : Token<Nothing>
object OpenSquareBracketToken : Token<Nothing>
object CloseSquareBracketToken : Token<Nothing>
object ClassToken : Token<Nothing>
object ExtendsToken : Token<Nothing>
object SuperToken : Token<Nothing>
object ThisToken : Token<Nothing>
object NewToken : Token<Nothing>


object SimpleAssignToken : StringBinaryOperatorToken {
    override val opt get() = "="
}

object LogicalAndToken : StringBinaryOperatorToken {
    override val opt get() = "&&"
}

object LogicalOrToken : StringBinaryOperatorToken {
    override val opt get() = "||"
}

object LogicalNotToken : StringBinaryOperatorToken {
    override val opt get() = "!"
}

interface BinaryOperatorToken<T> : Token<BinaryOperatorToken<T>> {
    @get:JsonValue
    val opt: T
    override val data get() = this

    companion object {
        fun <T, S> optOf(opt: T, v: Array<S>): S where S : BinaryOperatorToken<T>, S : Enum<*> {
            return v.firstOrNull { it.opt == opt } ?: throw RuntimeException()
        }
    }
}

interface StringBinaryOperatorToken : BinaryOperatorToken<String>

enum class AdditiveOperatorToken(override val opt: String) : StringBinaryOperatorToken {
    ADD("+"),
    SUB("-");
}

enum class MultiplicativeOperatorToken(override val opt: String) : StringBinaryOperatorToken {
    MUL("*"),
    DIV("/");
}

enum class ComplexAssignOperatorToken(override val opt: String) : StringBinaryOperatorToken {
    ADD_ASSIGN("+="),
    SUB_ASSIGN("-="),
    MUL_ASSIGN("*="),
    DIV_ASSIGN("/=");
}

enum class RelationalOperatorToken(override val opt: String) : StringBinaryOperatorToken {
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<=")
}

enum class EqualityOperatorToken(override val opt: String) : StringBinaryOperatorToken {
    EQUAL("=="),
    NOTEQUAL("!=")
}

enum class BooleanToken(override val opt: Boolean) : BinaryOperatorToken<Boolean> {
    TRUE(true),
    FALSE(false),
}
