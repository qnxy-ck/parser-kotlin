package com.qnxy.parser

import com.qnxy.token.*
import java.io.File

/**
 *
 * @author ck
 * 2022/11/24
 */
class Parser {

    private val tokenizer: Tokenizer
    private var lookahead: Token<*>?

    constructor(file: File) {
        this.tokenizer = Tokenizer(file)
        this.lookahead = this.tokenizer.nextToken()
    }

    constructor(codeStr: String) {
        this.tokenizer = Tokenizer(codeStr)
        this.lookahead = this.tokenizer.nextToken()
    }

    fun parse() = Program(this.statementList(CurlyBracketRToken))

    /**
     * StatementList
     *  : Statement
     *  | StatementList Statement -> Statement Statement Statement Statement
     *  ;
     */
    private fun statementList(curlyBracketR: CurlyBracketRToken? = null): List<Ast> {
        val statementList = mutableListOf<Ast>()
        statementList.add(this.statement())

        while (this.lookahead != null && this.lookahead != curlyBracketR) {
            statementList.add(this.statement())
        }
        return statementList.toList()
    }

    /**
     * Statement
     *  : ExpressionStatement
     *  | BlockStatement
     *  | EmptyStatement
     *  | VariableStatement
     *  | IfStatement
     *  | WhileStatement
     *  | FunctionDeclaration
     *  | ReturnStatement
     *  | ClassDeclaration
     *  ;
     */
    private fun statement(): Ast {
        return when (this.lookahead) {
            SemicolonToken -> this.emptyStatement()
            IfToken -> this.ifStatement()
            CurlyBracketLToken -> this.blockStatement()
            LetToken -> this.variableStatement()
            DefToken -> this.functionDeclaration()
            ClassToken -> this.classDeclaration()
            ReturnToken -> this.returnStatement()
            WhileToken, DoToken, ForToken -> this.iterationStatement()
            else -> this.expressionStatement()
        }
    }

    /**
     * ClassDeclaration
     *  : 'class' Identifier OptClassExtends BlockStatement
     *  ;
     */
    private fun classDeclaration(): Ast {
        this.consumeToken<ClassToken>()
        val id = this.identifier()

        val superClass = if (ExtendsToken.test()) this.classExtends() else null

        val body = this.blockStatement()
        return ClassDeclaration(id, superClass, body)
    }

    /**
     * ClassExtends
     *  : 'extends' Identifier
     *  ;
     */
    private fun classExtends(): Ast {
        this.consumeToken<ExtendsToken>()
        return this.identifier()
    }

    /**
     * FunctionDeclaration
     *  : 'def' Identifier '(' OptFormalParameterList ')' BlockStatement
     *  ;
     */
    private fun functionDeclaration(): Ast {
        this.consumeToken<DefToken>()
        val name = this.identifier()

        this.consumeToken<OpenParenthesisToken>()

        // OptFormalParameterList
        val params = if (CloseParenthesisToken.test()) emptyList() else this.formalParameterList()

        this.consumeToken<CloseParenthesisToken>()

        val body = this.blockStatement()
        return FunctionDeclaration(name, params, body)

    }

    /**
     * FormalParameterList
     *  : Identifier
     *  | FormalParameterList ',' Identifier
     *  ;
     */
    private fun formalParameterList(): List<Ast> {
        val params = mutableListOf<Ast>()

        while (true) {
            params.add(this.identifier())
            if (CommaToken.test()) this.consumeToken<CommaToken>() else break
        }

        return params
    }

    /**
     * ReturnStatement
     *  : 'return' OptExpression ';'
     *  ;
     */
    private fun returnStatement(): Ast {
        this.consumeToken<ReturnToken>()
        val argument = if (SemicolonToken.test()) null else this.expression()

        this.consumeToken<SemicolonToken>()
        return ReturnStatement(argument)
    }

    /**
     * IterationStatement
     *  : WhileStatement
     *  | DoStatement
     *  | ForStatement
     *  ;
     */
    private fun iterationStatement(): Ast {
        return when (this.lookahead) {
            WhileToken -> this.whileStatement()
            DoToken -> this.doWhileStatement()
            else -> this.forStatement()
        }
    }

    /**
     * WhileStatement
     *  : 'while' '(' Expression ')' Statement
     *  ;
     */
    private fun whileStatement(): Ast {
        this.consumeToken<WhileToken>()

        this.consumeToken<OpenParenthesisToken>()
        val test = this.expression()
        this.consumeToken<CloseParenthesisToken>()


        val body = this.statement()
        return WhileStatement(test, body)
    }

    /**
     * DoWhileStatement
     *  : 'do' '{' Statement '}' while '(' Expression ')' ';'
     *  ;
     */
    private fun doWhileStatement(): Ast {
        this.consumeToken<DoToken>()

        val body = this.statement()

        this.consumeToken<WhileToken>()

        this.consumeToken<OpenParenthesisToken>()
        val test = this.expression()
        this.consumeToken<CloseParenthesisToken>()

        this.consumeToken<SemicolonToken>()

        return DoWhileStatement(test, body)
    }

    /**
     * ForStatement
     *  : 'for' '(' OptForStatementInit ';' OptExpression ';' OptExpression ')' Statement
     */
    private fun forStatement(): Ast {
        this.consumeToken<ForToken>()
        this.consumeToken<OpenParenthesisToken>()

        // OptForStatementInit
        val init = if (SemicolonToken.test()) null else this.forStatementInit()
        this.consumeToken<SemicolonToken>()

        // OptExpression
        val test = if (SemicolonToken.test()) null else this.expression()
        this.consumeToken<SemicolonToken>()

        // OptExpression
        val update = if (CloseParenthesisToken.test()) null else this.expression()
        this.consumeToken<CloseParenthesisToken>()

        val body = this.statement()
        return ForStatement(init, test, update, body)
    }

    /**
     * ForStatementInit
     *  : VariableStatementInit
     *  | Expression
     *  ;
     */
    private fun forStatementInit(): Ast {
        return if (LetToken.test()) this.variableStatementInit() else this.expression()
    }

    /**
     * IfStatement
     *  : 'if' '(' Expression ')' Statement
     *  | 'if' '(' Expression ')' Statement 'else' Statement
     */
    private fun ifStatement(): IfStatement {
        this.consumeToken<IfToken>()

        this.consumeToken<OpenParenthesisToken>()
        val test = this.expression()
        this.consumeToken<CloseParenthesisToken>()

        val consequent = this.statement()

        val alternate = if (ElseToken.test()) {
            this.consumeToken<ElseToken>()
            this.statement()
        } else null

        return IfStatement(test, consequent, alternate)
    }

    /**
     * VariableStatementInit
     *  : 'let' VariableDeclarationList
     *  ;
     */
    private fun variableStatementInit(): Ast {
        this.consumeToken<LetToken>()
        return VariableStatement(this.variableDeclarationList())
    }

    /**
     * VariableStatement
     *  : VariableStatementInit ';'
     *  ;
     */
    private fun variableStatement(): Ast {
        return this.variableStatementInit().also { this.consumeToken<SemicolonToken>() }
    }

    /**
     * VariableDeclarationList
     *  : VariableDeclaration
     *  | VariableDeclarationList ',' VariableDeclaration
     *  ;
     */
    private fun variableDeclarationList(): List<VariableDeclaration> {
        val declarations = mutableListOf<VariableDeclaration>()

        while (true) {
            declarations.add(this.variableDeclaration())
            if (CommaToken.test()) this.consumeToken<CommaToken>() else break
        }
        return declarations
    }

    /**
     * VariableDeclaration
     *  : Identifier OptVariableInitializer
     *  ;
     */
    private fun variableDeclaration(): VariableDeclaration {
        val id = this.identifier()

        // OptVariableInitializer
        val init = if (CommaToken.test() || SemicolonToken.test()) null else this.variableInitializer()

        return VariableDeclaration(id, init)
    }

    /**
     * VariableInitializer
     *  : SIMPLE_ASSIGN AssignmentExpression
     *  ;
     */
    private fun variableInitializer(): Ast {
        this.consumeToken<SimpleAssignToken>()
        return this.assignmentExpression()
    }

    /**
     * EmptyStatement
     *  : ';'
     *  ;
     */
    private fun emptyStatement(): EmptyStatement {
        this.consumeToken<SemicolonToken>()
        return EmptyStatement
    }

    /**
     * BlockStatement
     *  : '{' OptStatementList '}'
     *  ;
     */
    private fun blockStatement(): Ast {
        this.consumeToken<CurlyBracketLToken>()

        val body = if (CurlyBracketRToken.test()) listOf() else this.statementList(CurlyBracketRToken)
        this.consumeToken<CurlyBracketRToken>()
        return BlockStatement(body)
    }

    /**
     * ExpressionStatement
     *  : Expression ';'
     *  ;
     */
    private fun expressionStatement(): Ast {
        val expression = this.expression()
        this.consumeToken<SemicolonToken>()
        return ExpressionStatement(expression)
    }

    /**
     * Expression
     *  : AssignmentExpression
     *  ;
     */
    private fun expression() = this.assignmentExpression()

    /**
     * AssignmentExpression
     *  : LogicalOrExpression
     *  | LeftHandSideExpression AssignmentOperator AssignmentExpression
     *  ;
     */
    private fun assignmentExpression(): Ast {
        val left = this.logicalOrExpression()
        if (!this.isAssignmentOperator()) return left

        return AssignmentExpression(
            this.assignmentOperator(),
            this.checkValidAssignmentTarget(left),
            this.assignmentExpression()
        )
    }

    /**
     * Identifier
     *  : IDENTIFIER
     *  ;
     */
    private fun identifier() = Identifier(this.consumeToken<IdentifierToken>().data)

    /**
     * Extra check whether its valid assignment target
     */
    private fun checkValidAssignmentTarget(left: Ast): Ast {
        return if (left is Identifier || left is MemberExpression) left else throw RuntimeException("Invalid left-hand side in assignment expression")
    }

    /**
     * Whether the token is an assignment operator
     */
    private fun isAssignmentOperator(): Boolean {
        return SimpleAssignToken.test() || this.lookahead is ComplexAssignOperatorToken
    }

    /**
     * AssignmentOperator
     *  : SIMPLE_ASSIGN
     *  | COMPLEX_ASSIGN
     *  ;
     */
    private fun assignmentOperator(): StringBinaryOperatorToken {
        return this.consumeToken()
    }

    /**
     * LogicalOrExpression
     *  : LogicalAndExpression LOGICAL_OR LogicalOrExpression
     *  | LogicalOrExpression
     *  ;
     */
    private fun logicalOrExpression() = this.genericOperatorExpression<LogicalOrToken>(false) { this.logicalAndExpression() }

    /**
     * LogicalAndExpression
     *  : EqualityExpression LOGICAL_AND LogicalAndExpression
     *  | EqualityExpression
     *  ;
     */
    private fun logicalAndExpression() = this.genericOperatorExpression<LogicalAndToken>(false) { this.equalityExpression() }

    /**
     * EQUALITY_OPERATOR: == !=
     *
     * EqualityExpression
     *  : RelationalExpression EQUALITY_OPERATOR EqualityExpression
     *  | RelationalExpression
     *  ;
     */
    private fun equalityExpression() = this.genericOperatorExpression<EqualityOperatorToken> { this.relationalExpression() }

    /**
     * RELATIONAL_OPERATOR: > >= < <=
     *
     * RelationalExpression
     *  : AdditiveExpression
     *  | AdditiveExpression RELATIONAL_OPERATOR RelationalExpression
     *  ;
     */
    private fun relationalExpression() = this.genericOperatorExpression<RelationalOperatorToken> { this.additiveExpression() }

    /**
     * AdditiveExpression
     *  : MultiplicativeExpression
     *  | AdditiveExpression ADDITIVE_OPERATOR MultiplicativeExpression -> MultiplicativeExpression ADDITIVE_OPERATOR MultiplicativeExpression ADDITIVE_OPERATOR MultiplicativeExpression
     */
    private fun additiveExpression() = this.genericOperatorExpression<AdditiveOperatorToken> { this.multiplicativeExpression() }

    /**
     * MultiplicativeExpression
     *  : UnaryExpression
     *  | MultiplicativeExpression MULTIPLICATIVE_OPERATOR UnaryExpression
     *  ;
     */
    private fun multiplicativeExpression() = this.genericOperatorExpression<MultiplicativeOperatorToken> { this.unaryExpression() }

    /**
     * Generic binary or logical expression
     */
    private inline fun <reified T : StringBinaryOperatorToken> genericOperatorExpression(isBinary: Boolean = true, builderBlock: () -> Ast): Ast {
        var left = builderBlock()

        while (this.lookahead is T) {
            val operatorToken = this.consumeToken<T>()

            val right = builderBlock()
            left = if (isBinary) BinaryExpression(operatorToken, left, right) else LogicalExpression(operatorToken, left, right)
        }

        return left
    }

    /**
     * UnaryExpression
     *  : LeftHandSideExpression
     *  | ADDITIVE_OPERATOR UnaryExpression
     *  | LOGICAL_NOT UnaryExpression
     *  ;
     */
    private fun unaryExpression(): Ast {
        val operator = when (this.lookahead) {
            is AdditiveOperatorToken -> this.consumeToken<AdditiveOperatorToken>()
            LogicalNotToken -> this.consumeToken<LogicalNotToken>()
            else -> null
        } ?: return this.leftHandSideExpression()

        return UnaryExpression(operator, this.unaryExpression())
    }

    /**
     * LeftHandSideExpression
     *  : CallMemberExpression
     *  ;
     */
    private fun leftHandSideExpression() = this.callMemberExpression()


    /**
     * CallMemberExpression
     *  : MemberExpression
     *  | CallExpression
     *  ;
     */
    private fun callMemberExpression(): Ast {
        // Super call:
        if (SuperToken.test()) return this.callExpression(this.`super`())

        // Member pat, might be part of a call;
        val member = this.memberExpression()

        // See if we have a call expression otherwise returns simple member expression
        return if (OpenParenthesisToken.test()) this.callExpression(member) else member
    }

    /**
     * CallExpression
     *  : Callee Arguments
     *  ;
     *
     * Callee
     *  : MemberExpression
     *  | CallExpression
     *  ;
     *
     */
    private tailrec fun callExpression(callee: Ast): Ast {
        val callExpression = CallExpression(callee, this.arguments())

        return if (OpenParenthesisToken.test()) this.callExpression(callExpression) else callExpression
    }

    /**
     * Arguments
     *  : '(' OptArgumentList ')'
     *  ;
     */
    private fun arguments(): List<Ast> {
        this.consumeToken<OpenParenthesisToken>()
        val argumentList = if (CloseParenthesisToken.test()) emptyList() else this.argumentList()

        this.consumeToken<CloseParenthesisToken>()
        return argumentList
    }

    /**
     * ArgumentList
     *  : AssignmentExpression
     *  | ArgumentList ',' AssignmentExpression
     *  ;
     */
    private fun argumentList(): List<Ast> {
        val argumentList = mutableListOf<Ast>()

        while (true) {
            argumentList.add(this.assignmentExpression())
            if (CommaToken.test()) {
                this.consumeToken<CommaToken>()
            } else break
        }

        return argumentList
    }

    /**
     * MemberExpression
     *  : PrimaryExpression
     *  | MemberExpression '.' Identifier
     *  | MemberExpression '[' Expression ']'
     *  ;
     */
    private fun memberExpression(): Ast {
        var obj = this.primaryExpression()

        while (true) {
            obj = when {
                // MemberExpression '.' Identifier
                DotToken.test() -> {
                    this.consumeToken<DotToken>()
                    val property = this.identifier()
                    MemberExpression(false, obj, property)
                }

                // MemberExpression '[' Expression ']'
                OpenSquareBracketToken.test() -> {
                    this.consumeToken<OpenSquareBracketToken>()
                    val property = this.expression()
                    this.consumeToken<CloseSquareBracketToken>()
                    MemberExpression(true, obj, property)
                }

                else -> break
            }
        }
        return obj
    }

    /**
     * PrimaryExpression
     *  : Literal
     *  | ParenthesizedExpression
     *  | LeftHandSideExpression
     *  | Identifier
     *  | ThisExpression
     *  | NewExpression
     *  ;
     */
    private fun primaryExpression(): Ast {
        if (this.isLiteral()) return this.literal()

        return when (this.lookahead) {
            OpenParenthesisToken -> this.parenthesizedExpression()
            is IdentifierToken -> this.identifier()
            ThisToken -> this.thisExpression()
            NewToken -> this.newExpression()
            else -> this.leftHandSideExpression()
        }
    }

    /**
     * NewExpression
     *  : 'new' MemberExpression Arguments
     *  ;
     */
    private fun newExpression(): Ast {
        this.consumeToken<NewToken>()
        return NewExpression(this.memberExpression(), this.arguments())
    }

    /**
     * ThisExpression
     *  : 'this'
     *  ;
     */
    private fun thisExpression(): Ast {
        this.consumeToken<ThisToken>()
        return ThisExpression
    }

    /**
     * Super
     *  : 'super'
     *  ;
     */
    private fun `super`(): Super {
        this.consumeToken<SuperToken>()
        return Super
    }

    /**
     * Whether the token is a literal
     */
    private fun isLiteral(): Boolean {
        return when (this.lookahead) {
            is NumberToken -> true
            is StringToken -> true
            is BooleanToken -> true
            NullToken -> true
            else -> false
        }
    }

    /**
     * ParenthesizedExpression
     *  : '(' Expression ')'
     *  ;
     */
    private fun parenthesizedExpression(): Ast {
        this.consumeToken<OpenParenthesisToken>()
        return this.expression().also { this.consumeToken<CloseParenthesisToken>() }
    }

    /**
     * Literal
     *  : NumericLiteral
     *  | StringLiteral
     *  | BooleanLiteral
     *  | NullLiteral
     *  ;
     */
    private fun literal(): Ast {
        return when (this.lookahead) {
            is NumberToken -> this.numericLiteral()
            is StringToken -> this.stringLiteral()
            is BooleanToken -> this.booleanLiteral()
            NullToken -> this.nullLiteral()
            else -> throw RuntimeException("Literal: unexpected literal production")
        }
    }

    /**
     * BooleanLiteral
     *  : true
     *  | false
     *  ;
     */
    private fun booleanLiteral(): Ast {
        return BooleanLiteral(this.consumeToken())
    }

    /**
     * NullLiteral
     *  : null
     *  ;
     */
    private fun nullLiteral(): NullLiteral {
        this.consumeToken<NullToken>()
        return NullLiteral
    }

    /**
     * StringLiteral
     */
    private fun stringLiteral(): StringLiteral {
        val token = this.consumeToken<StringToken>()
        return StringLiteral(token.data)
    }

    /**
     * NumericLiteral
     */
    private fun numericLiteral(): NumericLiteral {
        val token = this.consumeToken<NumberToken>()
        return NumericLiteral(token.data)
    }

    @Suppress("UnusedReceiverParameter")
    private inline fun <reified T : Token<*>> T.test(): Boolean {
        return this@Parser.lookahead is T
    }

    private inline fun <reified T : Token<*>> consumeToken(): T {
        val token = this.lookahead ?: throw RuntimeException("Unexpected end of input, expected: ${T::class.java.simpleName}")
        if (token !is T) throw RuntimeException("Unexpected token: ${token::class.java.simpleName}, expected: ${T::class.java.simpleName}")

        this.lookahead = this.tokenizer.nextToken()
        return token
    }


}



