package jp.opap.material.data;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.jaxrs.cfg.EndpointConfigBase;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterModifier;

import javax.servlet.*;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

/**
 * https://gist.github.com/komiya-atsushi/832a97c84ccae7cdfc2a より
 */
public class JavaScriptPrettyPrinter implements PrettyPrinter {
    enum State {
        ROOT_VALUE_SEPARATOR,
        START_OBJECT,
        END_OBJECT,
        OBJECT_ENTRY_SEPARATOR,
        OBJECT_FIELD_VALUES_SEPARATOR,
        START_ARRAY,
        END_ARRAY,
        ARRAY_VALUE_SEPARATOR,
        BEFORE_ARRAY_VALUES,
        BEFORE_OBJECT_ENTRIES
    }

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final char[] SPACES = new char[128];

    static {
        Arrays.fill(SPACES, ' ');
    }

    private final int numSpacesPerIndent;
    private int indentLevel;
    private State lastState;
    private Stack<Boolean> listContainsLiteralOnly = new Stack<>();

    /**
     * インデントのときのスペース個数を指定して、JsonStringifyPrettyPrinter オブジェクトを生成します。
     *
     * @param numSpacesPerIndent インデントのときのスペース個数を指定します
     */
    JavaScriptPrettyPrinter(int numSpacesPerIndent) {
        this.numSpacesPerIndent = numSpacesPerIndent;
    }

    private void indent(JsonGenerator jg) throws IOException {
        if (lastState != null)
            jg.writeRaw(LINE_SEPARATOR);

        int numSpacesToBeOutput = indentLevel * numSpacesPerIndent;
        while (numSpacesToBeOutput > SPACES.length) {
            jg.writeRaw(SPACES, 0, SPACES.length);
            numSpacesToBeOutput -= SPACES.length;
        }

        jg.writeRaw(SPACES, 0, numSpacesToBeOutput);
    }

    @Override
    public void writeRootValueSeparator(JsonGenerator jg) throws IOException {
        lastState = State.ROOT_VALUE_SEPARATOR;
    }

    @Override
    public void writeStartObject(JsonGenerator jg) throws IOException {
        if (lastState != State.OBJECT_FIELD_VALUES_SEPARATOR) {
            indent(jg);
        }
        jg.writeRaw("{");
        indentLevel++;

        if (!listContainsLiteralOnly.empty() && listContainsLiteralOnly.peek()) {
            listContainsLiteralOnly.pop();
            listContainsLiteralOnly.push(false);
        }

        lastState = State.START_OBJECT;
    }

    @Override
    public void writeEndObject(JsonGenerator jg, int nrOfEntries) throws IOException {
        indentLevel--;
        indent(jg);
        jg.writeRaw("}");

        lastState = State.END_OBJECT;
        if (indentLevel == 0)
            jg.writeRaw(LINE_SEPARATOR);
    }

    @Override
    public void writeObjectEntrySeparator(JsonGenerator jg) throws IOException {
        jg.writeRaw(",");
        indent(jg);

        lastState = State.OBJECT_ENTRY_SEPARATOR;
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator jg) throws IOException {
        jg.writeRaw(": ");

        lastState = State.OBJECT_FIELD_VALUES_SEPARATOR;
    }

    @Override
    public void writeStartArray(JsonGenerator jg) throws IOException {
        if (lastState != State.OBJECT_FIELD_VALUES_SEPARATOR) {
            indent(jg);
        }
        jg.writeRaw("[");
        indentLevel++;

        listContainsLiteralOnly.push(true);

        lastState = State.START_ARRAY;
    }

    @Override
    public void writeEndArray(JsonGenerator jg, int nrOfValues) throws IOException {
        indentLevel--;

        if (!listContainsLiteralOnly.pop()) {
            indent(jg);
        }
        jg.writeRaw("]");

        lastState = State.END_ARRAY;
    }

    @Override
    public void writeArrayValueSeparator(JsonGenerator jg) throws IOException {
        jg.writeRaw(", ");

        lastState = State.ARRAY_VALUE_SEPARATOR;
    }

    @Override
    public void beforeArrayValues(JsonGenerator jg) throws IOException {
        lastState = State.BEFORE_ARRAY_VALUES;
    }

    @Override
    public void beforeObjectEntries(JsonGenerator jg) throws IOException {
        indent(jg);
        lastState = State.BEFORE_OBJECT_ENTRIES;
    }

    static ObjectWriterModifier injector() {
        return new ObjectWriterModifier() {
            @Override
            public ObjectWriter modify(EndpointConfigBase<?> endpoint, MultivaluedMap<String, Object> responseHeaders,
                                       Object valueToWrite, ObjectWriter w, JsonGenerator g) throws IOException {
                return w.with(new JavaScriptPrettyPrinter(4));
            }
        };
    }

    public static class PrettyPrintFilter implements Filter {
        public static PrettyPrintFilter SINGLETON = new PrettyPrintFilter();

        PrettyPrintFilter() {}

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {}

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            ObjectWriterInjector.set(injector());
            chain.doFilter(request, response);
        }

        @Override
        public void destroy() {}
    }
}
