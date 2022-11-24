package com.qnxy.token

import java.io.File

/**
 *
 * @author ck
 * 2022/11/24
 */
internal class Tokenizer {
    private val text: String

    constructor(str: String) {
        this.text = str
    }

    constructor(file: File) {
        this.text = file.readText()
    }

    private var cursor = 0

    private val matchMap: Map<Regex, (String) -> Token?> = mapOf(
        // ------------------------------------------------------------------------------
        // 空格
        "\\s+".toRegex() to { null },

        // ------------------------------------------------------------------------------
        // 注释
        """//.*""".toRegex() to { null },
        """/\*[\s\S]*?\*/""".toRegex() to { null },

        // ------------------------------------------------------------------------------
        // 符号
        ";".toRegex() to { SemicolonToken },
        "\\{".toRegex() to { CurlyBracketLToken },
        "}".toRegex() to { CurlyBracketRToken },
        "\\(".toRegex() to { OpenParenthesisToken },
        "\\)".toRegex() to { CloseParenthesisToken },
        ",".toRegex() to { CommaToken },
        "\\.".toRegex() to { DotToken },
        "\\[".toRegex() to { OpenSquareBracketToken },
        "]".toRegex() to { CloseSquareBracketToken },

        // ------------------------------------------------------------------------------
        // 关键字
        "\\b(let)\\b".toRegex() to { LetToken },
        "\\b(if)\\b".toRegex() to { IfToken },
        "\\b(else)\\b".toRegex() to { ElseToken },
        "\\b(true)\\b".toRegex() to { BooleanToken.TRUE },
        "\\b(false)\\b".toRegex() to { BooleanToken.FALSE },
        "\\b(null)\\b".toRegex() to { NullToken },
        "\\b(while)\\b".toRegex() to { WhileToken },
        "\\b(do)\\b".toRegex() to { DoToken },
        "\\b(for)\\b".toRegex() to { ForToken },
        "\\b(def)\\b".toRegex() to { DefToken },
        "\\b(return)\\b".toRegex() to { ReturnToken },
        "\\b(class)\\b".toRegex() to { ClassToken },
        "\\b(this)\\b".toRegex() to { ThisToken },
        "\\b(new)\\b".toRegex() to { NewToken },
        "\\b(super)\\b".toRegex() to { SuperToken },
        "\\b(extends)\\b".toRegex() to { ExtendsToken },

        // ------------------------------------------------------------------------------
        // 数字
        "\\d+".toRegex() to { NumberToken(it.toInt()) },

        // ------------------------------------------------------------------------------
        // 标识符
        "\\w+".toRegex() to ::IdentifierToken,

        // ------------------------------------------------------------------------------
        // 等式符号
        "[=!]=".toRegex() to EqualityOperatorToken.values()::optOf,

        // ------------------------------------------------------------------------------
        // 赋值符号
        "=".toRegex() to { SimpleAssignToken },
        "[*/+\\-]=".toRegex() to ComplexAssignOperatorToken.values()::optOf,

        // ------------------------------------------------------------------------------
        // 关系符号
        "[<>]=?".toRegex() to RelationalOperatorToken.values()::optOf,

        // ------------------------------------------------------------------------------
        // 数学运算符号
        "[+-]".toRegex() to AdditiveOperatorToken.values()::optOf,
        "[*/]".toRegex() to MultiplicativeOperatorToken.values()::optOf,

        // ------------------------------------------------------------------------------
        // 逻辑符号
        "&&".toRegex() to { LogicalAndToken },
        "\\|\\|".toRegex() to { LogicalOrToken },
        "!".toRegex() to { LogicalNotToken },


        // ------------------------------------------------------------------------------
        // 字符串
        """"[^"]*"""".toRegex() to { StringToken(it.substring(1, it.length - 1)) },
        """'[^"]*'""".toRegex() to { StringToken(it.substring(1, it.length - 1)) }


    )

    tailrec fun nextToken(): Token? {
        if (cursor >= this.text.length) return null

        for ((reg, funToken) in matchMap) {
            val mr = reg.matchAt(text, this.cursor) ?: continue

            this.cursor = mr.range.last + 1
            return funToken(mr.value) ?: this.nextToken()
        }

        throw RuntimeException("""Unexpected token: "${text[this.cursor]}"""")
    }


}


