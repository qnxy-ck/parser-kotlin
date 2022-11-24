package com.qnxy.parser

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.qnxy.token.BooleanToken
import com.qnxy.token.StringBinaryOperatorToken


/**
 *
 * @author ck
 * 2022/11/24
 */
@JsonPropertyOrder("type")
interface Ast {
    @Suppress("unused")
    fun getType(): String = this::class.java.simpleName
}

@JvmInline
value class Program(val body: List<Ast>?) : Ast

@JvmInline
value class NumericLiteral(val value: Int) : Ast

@JvmInline
value class StringLiteral(val value: String) : Ast
object NullLiteral : Ast

@JvmInline
value class BooleanLiteral(val value: BooleanToken) : Ast

@JvmInline
value class ExpressionStatement(val expression: Ast) : Ast

@JvmInline
value class BlockStatement(val body: List<Ast>?) : Ast
object EmptyStatement : Ast

@JvmInline
value class Identifier(val name: String) : Ast
data class AssignmentExpression(val operator: StringBinaryOperatorToken, val left: Ast, val right: Ast) : Ast

@JvmInline
value class VariableStatement(val declarations: List<VariableDeclaration>) : Ast
data class VariableDeclaration(val id: Ast, val init: Ast? = null) : Ast
data class IfStatement(val test: Ast, val consequent: Ast, val alternate: Ast? = null) : Ast
data class WhileStatement(val test: Ast, val body: Ast) : Ast
data class DoWhileStatement(val test: Ast, val body: Ast) : Ast
data class ForStatement(val init: Ast? = null, val test: Ast? = null, val update: Ast? = null, val body: Ast) : Ast

data class BinaryExpression(val operator: StringBinaryOperatorToken, val left: Ast, val right: Ast) : Ast
data class LogicalExpression(val operator: StringBinaryOperatorToken, val left: Ast, val right: Ast) : Ast
data class UnaryExpression(val operator: StringBinaryOperatorToken, val argument: Ast) : Ast

data class FunctionDeclaration(val name: Ast, val params: List<Ast>, val body: Ast) : Ast
data class ReturnStatement(val argument: Ast?) : Ast

data class MemberExpression(val computed: Boolean, val obj: Ast, val property: Ast) : Ast
data class CallExpression(val callee: Ast, val arguments: List<Ast>) : Ast

data class ClassDeclaration(val id: Ast, val superClass: Ast?, val body: Ast) : Ast
object ThisExpression : Ast
object Super : Ast
data class NewExpression(val callee: Ast, val arguments: List<Ast>) : Ast