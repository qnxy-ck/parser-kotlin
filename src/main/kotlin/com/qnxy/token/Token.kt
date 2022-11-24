package com.qnxy.token

import com.fasterxml.jackson.annotation.JsonValue

/**
 *
 * @author ck
 * 2022/11/24
 */
interface Token
interface DataToken<T> : Token {
    val data: T
}

@JvmInline
value class NumberToken(override val data: Int) : DataToken<Int>

@JvmInline
value class StringToken(override val data: String) : DataToken<String>

@JvmInline
value class IdentifierToken(override val data: String) : DataToken<String>

object SemicolonToken : Token
object CurlyBracketLToken : Token
object CurlyBracketRToken : Token
object OpenParenthesisToken : Token
object CloseParenthesisToken : Token
object CommaToken : Token
object LetToken : Token
object IfToken : Token
object ElseToken : Token
object NullToken : Token
object WhileToken : Token
object DoToken : Token
object ForToken : Token
object DefToken : Token
object ReturnToken : Token
object DotToken : Token
object OpenSquareBracketToken : Token
object CloseSquareBracketToken : Token
object ClassToken : Token
object ExtendsToken : Token
object SuperToken : Token
object ThisToken : Token
object NewToken : Token

interface BinaryOperatorToken<T> : DataToken<T> {
    @get:JsonValue
    override val data: T
}

infix fun <T, S> Array<T>.optOf(opt: S): T where  T : BinaryOperatorToken<S>, T : Enum<*> {
    return this.firstOrNull { it.data == opt } ?: throw RuntimeException()
}

abstract class SimpleStringOperatorToken(override val data: String) : BinaryOperatorToken<String>

object SimpleAssignToken : SimpleStringOperatorToken("=")
object LogicalAndToken : SimpleStringOperatorToken("&&")
object LogicalOrToken : SimpleStringOperatorToken("||")
object LogicalNotToken : SimpleStringOperatorToken("!")

enum class AdditiveOperatorToken(override val data: String) : BinaryOperatorToken<String> {
    ADD("+"),
    SUB("-");
}

enum class MultiplicativeOperatorToken(override val data: String) : BinaryOperatorToken<String> {
    MUL("*"),
    DIV("/");
}

enum class ComplexAssignOperatorToken(override val data: String) : BinaryOperatorToken<String> {
    ADD_ASSIGN("+="),
    SUB_ASSIGN("-="),
    MUL_ASSIGN("*="),
    DIV_ASSIGN("/=");
}

enum class RelationalOperatorToken(override val data: String) : BinaryOperatorToken<String> {
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<=")
}

enum class EqualityOperatorToken(override val data: String) : BinaryOperatorToken<String> {
    EQUAL("=="),
    NOTEQUAL("!=")
}

enum class BooleanToken(override val data: Boolean) : BinaryOperatorToken<Boolean> {
    TRUE(true),
    FALSE(false),
}
