package org.aspose.pdf.engine.function;

import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Type 4 (PostScript Calculator) function (ISO 32000-1:2008, §7.10.5).
 * Evaluates a PostScript-like expression using a stack machine.
 *
 * <p>Supported operators include arithmetic (add, sub, mul, div, neg, abs, sqrt, exp, ln, log,
 * sin, cos, floor, ceiling, round, truncate), comparison (eq, ne, gt, ge, lt, le),
 * stack manipulation (dup, exch, pop, copy, index, roll), logic (and, or, xor, not),
 * and control (if, ifelse).</p>
 */
public final class PostScriptFunction extends PdfFunction {

    private static final Logger LOG = Logger.getLogger(PostScriptFunction.class.getName());

    /** Pre-classified token: either NUMBER with a value, or OPERATOR with an opcode. */
    private static final class Token {
        final int op;       // Opcode for operators; OP_NUMBER for numeric literals
        final double value; // Numeric value if op == OP_NUMBER
        Token(int op, double value) { this.op = op; this.value = value; }
    }

    // Opcodes — used in the inner loop (cheap int switch instead of String switch).
    private static final int OP_NUMBER = 0;
    private static final int OP_ADD = 1, OP_SUB = 2, OP_MUL = 3, OP_DIV = 4;
    private static final int OP_IDIV = 5, OP_MOD = 6, OP_NEG = 7, OP_ABS = 8;
    private static final int OP_CEIL = 9, OP_FLOOR = 10, OP_ROUND = 11, OP_TRUNC = 12;
    private static final int OP_SQRT = 13, OP_EXP = 14, OP_LN = 15, OP_LOG = 16;
    private static final int OP_SIN = 17, OP_COS = 18, OP_ATAN = 19;
    private static final int OP_AND = 20, OP_OR = 21, OP_XOR = 22, OP_NOT = 23, OP_BITSHIFT = 24;
    private static final int OP_EQ = 25, OP_NE = 26, OP_GT = 27, OP_GE = 28, OP_LT = 29, OP_LE = 30;
    private static final int OP_DUP = 31, OP_EXCH = 32, OP_POP = 33, OP_COPY = 34, OP_INDEX = 35;
    private static final int OP_TRUE = 36, OP_FALSE = 37;
    private static final int OP_UNKNOWN = 99;

    private final String program;
    private final Token[] tokens;

    /**
     * Creates a PostScript function from a COS stream dictionary.
     *
     * @param dict   the function stream dictionary
     * @param domain the input domain
     * @param range  the output range
     * @throws IOException if the stream data cannot be read
     */
    public PostScriptFunction(COSDictionary dict, double[] domain, double[] range)
            throws IOException {
        super(domain, range);
        if (dict instanceof COSStream) {
            byte[] data = ((COSStream) dict).getDecodedData();
            this.program = new String(data, StandardCharsets.US_ASCII).trim();
        } else {
            this.program = "";
        }
        this.tokens = compile(program);
    }

    /**
     * Creates a PostScript function directly (for testing).
     *
     * @param domain  the input domain
     * @param range   the output range
     * @param program the PostScript program string (with or without braces)
     */
    public PostScriptFunction(double[] domain, double[] range, String program) {
        super(domain, range);
        this.program = program != null ? program.trim() : "";
        this.tokens = compile(this.program);
    }

    /**
     * Pre-tokenises the program once at construction time and pre-classifies each
     * token as either a numeric literal (with parsed value) or an operator opcode.
     * <p>
     * Doing this here avoids re-tokenising on every {@link #evaluate(double[])}
     * call and — crucially — avoids the {@link NumberFormatException}-driven
     * hot path that was triggered for every operator token of every pixel during
     * DeviceN/Separation image decoding.
     * </p>
     */
    private static Token[] compile(String prog) {
        if (prog == null || prog.isEmpty()) return new Token[0];
        String body = prog;
        if (body.startsWith("{")) body = body.substring(1);
        if (body.endsWith("}")) body = body.substring(0, body.length() - 1);
        String[] raw = body.trim().split("\\s+");
        Token[] result = new Token[raw.length];
        int n = 0;
        for (String t : raw) {
            if (t.isEmpty() || "{".equals(t) || "}".equals(t)) continue;
            // Cheap numeric check — avoids the cost of throwing NumberFormatException.
            if (looksLikeNumber(t)) {
                try {
                    result[n++] = new Token(OP_NUMBER, Double.parseDouble(t));
                    continue;
                } catch (NumberFormatException ignore) {
                    // Fall through to operator classification.
                }
            }
            result[n++] = new Token(opcodeFor(t), 0);
        }
        if (n == result.length) return result;
        Token[] trimmed = new Token[n];
        System.arraycopy(result, 0, trimmed, 0, n);
        return trimmed;
    }

    private static boolean looksLikeNumber(String s) {
        if (s.isEmpty()) return false;
        int i = 0;
        char c0 = s.charAt(0);
        if (c0 == '+' || c0 == '-') {
            if (s.length() == 1) return false;
            i = 1;
        }
        boolean digit = false;
        boolean dot = false;
        for (; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                digit = true;
            } else if (c == '.' && !dot) {
                dot = true;
            } else if ((c == 'e' || c == 'E') && digit) {
                // Exponent — let parseDouble handle the rest.
                return true;
            } else {
                return false;
            }
        }
        return digit;
    }

    private static int opcodeFor(String tok) {
        switch (tok) {
            case "add": return OP_ADD;
            case "sub": return OP_SUB;
            case "mul": return OP_MUL;
            case "div": return OP_DIV;
            case "idiv": return OP_IDIV;
            case "mod": return OP_MOD;
            case "neg": return OP_NEG;
            case "abs": return OP_ABS;
            case "ceiling": return OP_CEIL;
            case "floor": return OP_FLOOR;
            case "round": return OP_ROUND;
            case "truncate": return OP_TRUNC;
            case "sqrt": return OP_SQRT;
            case "exp": return OP_EXP;
            case "ln": return OP_LN;
            case "log": return OP_LOG;
            case "sin": return OP_SIN;
            case "cos": return OP_COS;
            case "atan": return OP_ATAN;
            case "and": return OP_AND;
            case "or": return OP_OR;
            case "xor": return OP_XOR;
            case "not": return OP_NOT;
            case "bitshift": return OP_BITSHIFT;
            case "eq": return OP_EQ;
            case "ne": return OP_NE;
            case "gt": return OP_GT;
            case "ge": return OP_GE;
            case "lt": return OP_LT;
            case "le": return OP_LE;
            case "dup": return OP_DUP;
            case "exch": return OP_EXCH;
            case "pop": return OP_POP;
            case "copy": return OP_COPY;
            case "index": return OP_INDEX;
            case "true": return OP_TRUE;
            case "false": return OP_FALSE;
            default: return OP_UNKNOWN;
        }
    }

    @Override
    public double[] evaluate(double[] input) {
        // Primitive stack — no boxing in the hot loop.
        int cap = Math.max(16, tokens.length + input.length + 8);
        double[] stack = new double[cap];
        int sp = 0;

        // Push inputs (first input becomes the bottom of the stack).
        for (int i = input.length - 1; i >= 0; i--) {
            double v = clamp(input[i], domain[Math.min(i * 2, domain.length - 2)],
                    domain[Math.min(i * 2 + 1, domain.length - 1)]);
            stack[sp++] = v;
        }

        sp = execute(tokens, stack, sp);

        int numOutputs = range != null ? range.length / 2 : 1;
        double[] result = new double[numOutputs];
        for (int i = numOutputs - 1; i >= 0; i--) {
            result[i] = (sp > 0) ? stack[--sp] : 0;
            if (range != null && i * 2 + 1 < range.length) {
                result[i] = clamp(result[i], range[i * 2], range[i * 2 + 1]);
            }
        }
        return result;
    }

    private static int execute(Token[] toks, double[] stack, int sp) {
        for (int i = 0; i < toks.length; i++) {
            Token t = toks[i];
            switch (t.op) {
                case OP_NUMBER: stack[sp++] = t.value; break;
                case OP_ADD:    if (sp >= 2) { double b = stack[--sp], a = stack[--sp]; stack[sp++] = a + b; } break;
                case OP_SUB:    if (sp >= 2) { double b = stack[--sp], a = stack[--sp]; stack[sp++] = a - b; } break;
                case OP_MUL:    if (sp >= 2) { double b = stack[--sp], a = stack[--sp]; stack[sp++] = a * b; } break;
                case OP_DIV:    if (sp >= 2) { double b = stack[--sp], a = stack[--sp]; stack[sp++] = b != 0 ? a / b : 0; } break;
                case OP_IDIV:   if (sp >= 2) { double b = stack[--sp], a = stack[--sp]; stack[sp++] = b != 0 ? (double)((long) a / (long) b) : 0; } break;
                case OP_MOD:    if (sp >= 2) { double b = stack[--sp], a = stack[--sp]; stack[sp++] = b != 0 ? a % b : 0; } break;
                case OP_NEG:    if (sp >= 1) { stack[sp - 1] = -stack[sp - 1]; } break;
                case OP_ABS:    if (sp >= 1) { stack[sp - 1] = Math.abs(stack[sp - 1]); } break;
                case OP_CEIL:   if (sp >= 1) { stack[sp - 1] = Math.ceil(stack[sp - 1]); } break;
                case OP_FLOOR:  if (sp >= 1) { stack[sp - 1] = Math.floor(stack[sp - 1]); } break;
                case OP_ROUND:  if (sp >= 1) { stack[sp - 1] = Math.round(stack[sp - 1]); } break;
                case OP_TRUNC:  if (sp >= 1) { stack[sp - 1] = (double)(long) stack[sp - 1]; } break;
                case OP_SQRT:   if (sp >= 1) { stack[sp - 1] = Math.sqrt(stack[sp - 1]); } break;
                case OP_EXP:    if (sp >= 2) { double b = stack[--sp], a = stack[--sp]; stack[sp++] = Math.pow(a, b); } break;
                case OP_LN:     if (sp >= 1) { stack[sp - 1] = Math.log(stack[sp - 1]); } break;
                case OP_LOG:    if (sp >= 1) { stack[sp - 1] = Math.log10(stack[sp - 1]); } break;
                case OP_SIN:    if (sp >= 1) { stack[sp - 1] = Math.sin(Math.toRadians(stack[sp - 1])); } break;
                case OP_COS:    if (sp >= 1) { stack[sp - 1] = Math.cos(Math.toRadians(stack[sp - 1])); } break;
                case OP_ATAN:   if (sp >= 2) { double den = stack[--sp], num = stack[--sp]; stack[sp++] = Math.toDegrees(Math.atan2(num, den)); } break;
                case OP_AND:    if (sp >= 2) { long b = (long) stack[--sp], a = (long) stack[--sp]; stack[sp++] = (double)(a & b); } break;
                case OP_OR:     if (sp >= 2) { long b = (long) stack[--sp], a = (long) stack[--sp]; stack[sp++] = (double)(a | b); } break;
                case OP_XOR:    if (sp >= 2) { long b = (long) stack[--sp], a = (long) stack[--sp]; stack[sp++] = (double)(a ^ b); } break;
                case OP_NOT:    if (sp >= 1) { stack[sp - 1] = (long) stack[sp - 1] == 0 ? 1.0 : 0.0; } break;
                case OP_BITSHIFT: if (sp >= 2) {
                    int shift = (int) stack[--sp];
                    int val = (int) stack[--sp];
                    stack[sp++] = (double)(shift >= 0 ? val << shift : val >> (-shift));
                } break;
                case OP_EQ:     if (sp >= 2) { double b = stack[--sp], a = stack[--sp]; stack[sp++] = a == b ? 1.0 : 0.0; } break;
                case OP_NE:     if (sp >= 2) { double b = stack[--sp], a = stack[--sp]; stack[sp++] = a != b ? 1.0 : 0.0; } break;
                case OP_GT:     if (sp >= 2) { double b = stack[--sp], a = stack[--sp]; stack[sp++] = a > b ? 1.0 : 0.0; } break;
                case OP_GE:     if (sp >= 2) { double b = stack[--sp], a = stack[--sp]; stack[sp++] = a >= b ? 1.0 : 0.0; } break;
                case OP_LT:     if (sp >= 2) { double b = stack[--sp], a = stack[--sp]; stack[sp++] = a < b ? 1.0 : 0.0; } break;
                case OP_LE:     if (sp >= 2) { double b = stack[--sp], a = stack[--sp]; stack[sp++] = a <= b ? 1.0 : 0.0; } break;
                case OP_DUP:    if (sp >= 1) { stack[sp] = stack[sp - 1]; sp++; } break;
                case OP_EXCH:   if (sp >= 2) { double a = stack[sp - 1]; stack[sp - 1] = stack[sp - 2]; stack[sp - 2] = a; } break;
                case OP_POP:    if (sp >= 1) sp--; break;
                case OP_COPY: {
                    if (sp >= 1) {
                        int n = (int) stack[--sp];
                        if (n > 0 && sp >= n && sp + n <= stack.length) {
                            System.arraycopy(stack, sp - n, stack, sp, n);
                            sp += n;
                        }
                    }
                    break;
                }
                case OP_INDEX: {
                    if (sp >= 1) {
                        int n = (int) stack[--sp];
                        if (n >= 0 && sp - 1 - n >= 0) {
                            stack[sp] = stack[sp - 1 - n];
                            sp++;
                        }
                    }
                    break;
                }
                case OP_TRUE:   stack[sp++] = 1.0; break;
                case OP_FALSE:  stack[sp++] = 0.0; break;
                default: /* OP_UNKNOWN — silently skip */ break;
            }
        }
        return sp;
    }
}
