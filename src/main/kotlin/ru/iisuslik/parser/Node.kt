package ru.iisuslik.parser

import java.util.concurrent.locks.Condition

interface Node {
    fun getTree(prefix: String, isTail: Boolean): String
}

///

class ExprNode(val l: VNode, val op: OToken? = null, val r: ExprNode? = null) : Node {

    override fun getTree(prefix: String, isTail: Boolean): String {
        if (op != null && r != null) {
            var res = prefix + (if (isTail) "└── " else "├── ") + op + '\n'
            res += l.getTree(prefix + (if (isTail) "    " else "│   "), false)
            res += r.getTree(prefix + (if (isTail) "    " else "│   "), true)
            return res
        } else {
            return l.getTree(prefix, isTail)
        }
    }

    override fun toString(): String {
        if (op == null) {
            return l.toString()
        }
        return "$l $op $r"
    }
}

interface VNode : Node {

}

class CallNode(var name: INode, var args: NodeList<ExprNode>) : VNode {
    override fun getTree(prefix: String, isTail: Boolean): String {
        var res = prefix + (if (isTail) "└── " else "├── ") + "Call" + '\n'
        res += name.getTree(prefix + (if (isTail) "    " else "│   "), false)
        res += args.getTree(prefix + (if (isTail) "    " else "│   "), true, "Call Args")
        return res
    }

    override fun toString(): String {
        return "$name ($args)"
    }
}

class BoolNode(var value: BToken) : VNode {
    override fun getTree(prefix: String, isTail: Boolean): String {
        return prefix + (if (isTail) "└── " else "├── ") + value + '\n'
    }

    override fun toString(): String {
        return value.toString()
    }
}

class NumNode(var value: NToken) : VNode {
    override fun getTree(prefix: String, isTail: Boolean): String {
        var res = prefix + (if (isTail) "└── " else "├── ") + value + '\n'
        return res
    }

    override fun toString(): String {
        return value.toString()
    }
}

class INode(var ident: IToken) : VNode {
    override fun getTree(prefix: String, isTail: Boolean): String {
        var res = prefix + (if (isTail) "└── " else "├── ") + ident + '\n'
        return res
    }

    override fun toString(): String {
        return ident.toString()
    }
}

class FunNode(var keyWord: KToken, var args: NodeList<ExprNode>, var body: NodeList<StNode>) : VNode {
    override fun getTree(prefix: String, isTail: Boolean): String {
        var res = prefix + (if (isTail) "└── " else "├── ") + keyWord + '\n'
        res += args.getTree(prefix + (if (isTail) "    " else "│   "), false, "Function args")
        res += body.getTree(prefix + (if (isTail) "    " else "│   "), true, "Function body")
        return res
    }

    override fun toString(): String {
        return "$keyWord ($args) {\n$body}"
    }
}

class ExprVNode(var node: ExprNode) : VNode {

    override fun getTree(prefix: String, isTail: Boolean): String {
        return node.getTree(prefix, isTail)
    }

    override fun toString(): String {
        return "($node)"
    }
}

class ReadNode(var keyWord: KToken) : VNode {
    override fun getTree(prefix: String, isTail: Boolean): String {
        return prefix + (if (isTail) "└── " else "├── ") + keyWord + '\n'
    }

    override fun toString(): String {
        return "$keyWord"
    }
}

///

interface StNode : Node

class CallStNode(val callNode: CallNode) : StNode {
    override fun getTree(prefix: String, isTail: Boolean): String {
        return callNode.getTree(prefix, isTail)
    }

    override fun toString(): String {
        return callNode.toString()
    }
}

class IfNode(var ifWord: KToken, var condition: ExprNode, var ifBody: NodeList<StNode>,
             var elseWord: KToken? = null, var elseBody: NodeList<StNode>? = null) : StNode {

    override fun getTree(prefix: String, isTail: Boolean): String {
        var res = prefix + (if (isTail) "└── " else "├── ") + ifWord + '\n'
        res += condition.getTree(prefix + (if (isTail) "    " else "│   "), false)
        if (elseWord == null) {
            res += ifBody.getTree(prefix + (if (isTail) "    " else "│   "), true, "If body")
        } else {
            res += ifBody.getTree(prefix + (if (isTail) "    " else "│   "), false, "If body")
            res += prefix + "    ├── " + elseWord + '\n'
            res += ifBody.getTree(prefix + (if (isTail) "    " else "│   "), true, "Else body")
        }
        return res
    }

    override fun toString(): String {
        return "$ifWord ($condition) {\n$ifBody}\n$elseWord{\n$elseBody}\n"
    }
}

class WhileNode(var whileWord: KToken, var condition: ExprNode, var whileBody: NodeList<StNode>) : StNode {

    override fun getTree(prefix: String, isTail: Boolean): String {
        var res = prefix + (if (isTail) "└── " else "├── ") + whileWord + '\n'
        res += condition.getTree(prefix + (if (isTail) "    " else "│   "), false)
        res += whileBody.getTree(prefix + (if (isTail) "    " else "│   "), true, "While body")
        return res
    }

    override fun toString(): String {
        return "$whileWord ($condition) {\n$whileBody}\n"
    }
}

class ExprStNode(var node: ExprNode) : StNode {

    override fun getTree(prefix: String, isTail: Boolean): String {
        return node.getTree(prefix, isTail)
    }

    override fun toString(): String {
        return "$node;\n"
    }
}

class WriteNode(var writeWord: KToken, var expr: ExprNode) : StNode {

    override fun getTree(prefix: String, isTail: Boolean): String {
        var res = prefix + (if (isTail) "└── " else "├── ") + writeWord + '\n'
        res += expr.getTree(prefix + (if (isTail) "    " else "│   "), true)
        return res
    }

    override fun toString(): String {
        return "$writeWord ($expr);\n"
    }
}

class ANode(var varName: INode, var EqOp: OToken, var expr: ExprNode) : StNode {

    override fun getTree(prefix: String, isTail: Boolean): String {
        var res = prefix + (if (isTail) "└── " else "├── ") + "Assignment" + '\n'
        res += varName.getTree(prefix + (if (isTail) "    " else "│   "), false)
        res += expr.getTree(prefix + (if (isTail) "    " else "│   "), true)
        return res
    }

    override fun toString(): String {
        return "$varName = $expr;\n"
    }
}

class RetNode(var returnWord: KToken, var expr: ExprNode) : StNode {

    override fun getTree(prefix: String, isTail: Boolean): String {
        var res = prefix + (if (isTail) "└── " else "├── ") + returnWord + '\n'
        res += expr.getTree(prefix + (if (isTail) "    " else "│   "), true)
        return res
    }


    override fun toString(): String {
        return "$returnWord $expr;\n"
    }
}

///

class MainNode(var statements: NodeList<StNode>) : Node {

    fun getTree(): String {
        return getTree("", true)
    }

    override fun getTree(prefix: String, isTail: Boolean): String {
        return statements.getTree(prefix + (if (isTail) "    " else "│   "), true, "Main")
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (node in statements.list) {
            sb.append(node.toString() + "\n")
        }
        return sb.toString()
    }
}

class NodeList<T>(val list: List<T>) {
    fun getTree(prefix: String, isTail: Boolean, name: String): String {
        var res = prefix + (if (isTail) "└── " else "├── ") + name + '\n'
        for (i in 0 until list.size - 1) {
            res += (list[i] as Node).getTree(prefix + (if (isTail) "    " else "│   "), false)
        }
        if (list.size > 0) {
            res += (list[list.size - 1] as Node).getTree(prefix + (if (isTail) "    " else "│   "), true)
        }
        return res
    }

}


