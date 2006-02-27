/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package javax.mail.internet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;// Represents lists in things like

import org.apache.geronimo.mail.util.ASCIIUtil;
import org.apache.geronimo.mail.util.RFC2231Encoder;
import org.apache.geronimo.mail.util.SessionUtil;

// Content-Type: text/plain;charset=klingon
//
// The ;charset=klingon is the parameter list, may have more of them with ';'

/**
 * @version $Rev$ $Date$
 */
public class ParameterList {
    private static final String MIME_ENCODEPARAMETERS = "mail.mime.encodeparameters";
    private static final String MIME_DECODEPARAMETERS = "mail.mime.decodeparameters";
    private static final String MIME_DECODEPARAMETERS_STRICT = "mail.mime.decodeparameters.strict";

    private static final int HEADER_SIZE_LIMIT = 76;

    private Map _parameters = new HashMap();

    private boolean encodeParameters = false;
    private boolean decodeParameters = false;
    private boolean decodeParametersStrict = false;

    public ParameterList() {
        // figure out how parameter handling is to be performed.
        getInitialProperties();
    }

    public ParameterList(String list) throws ParseException {
        // figure out how parameter handling is to be performed.
        getInitialProperties();
        // get a token parser for the type information
        HeaderTokenizer tokenizer = new HeaderTokenizer(list, HeaderTokenizer.MIME);
        while (true) {
            HeaderTokenizer.Token token = tokenizer.next();

            switch (token.getType()) {
                // the EOF token terminates parsing.
                case HeaderTokenizer.Token.EOF:
                    return;

                // each new parameter is separated by a semicolon, including the first, which separates
                // the parameters from the main part of the header.
                case ';':
                    // the next token needs to be a parameter name
                    token = tokenizer.next();
                    // allow a trailing semicolon on the parameters.
                    if (token.getType() == HeaderTokenizer.Token.EOF) {
                        return;
                    }

                    if (token.getType() != HeaderTokenizer.Token.ATOM) {
                        throw new ParseException("Invalid parameter name: " + token.getValue());
                    }

                    // get the parameter name as a lower case version for better mapping.
                    String name = token.getValue().toLowerCase();

                    token = tokenizer.next();

                    // parameters are name=value, so we must have the "=" here.
                    if (token.getType() != '=') {
                        throw new ParseException("Missing '='");
                    }

                    // now the value, which may be an atom or a literal
                    token = tokenizer.next();

                    if (token.getType() != HeaderTokenizer.Token.ATOM && token.getType() != HeaderTokenizer.Token.QUOTEDSTRING) {
                        throw new ParseException("Invalid parameter value: " + token.getValue());
                    }

                    String value = token.getValue();
                    String encodedValue = null;

                    // we might have to do some additional decoding.  A name that ends with "*"
                    // is marked as being encoded, so if requested, we decode the value.
                    if (decodeParameters && name.endsWith("*")) {
                        // the name needs to be pruned of the marker, and we need to decode the value.
                        name = name.substring(0, name.length() - 1);
                        // get a new decoder
                        RFC2231Encoder decoder = new RFC2231Encoder(HeaderTokenizer.MIME);

                        try {
                            // decode the value
                            encodedValue = decoder.decode(value);
                        } catch (Exception e) {
                            // if we're doing things strictly, then raise a parsing exception for errors.
                            // otherwise, leave the value in its current state.
                            if (decodeParametersStrict) {
                                throw new ParseException("Invalid RFC2231 encoded parameter");
                            }
                        }
                    }
                    _parameters.put(name, new ParameterValue(name, value, encodedValue));

                    break;

                default:
                    throw new ParseException("Missing ';'");

            }
        }
    }

    /**
     * Get the initial parameters that control parsing and values.
     * These parameters are controlled by System properties.
     */
    private void getInitialProperties() {
        decodeParameters = SessionUtil.getBooleanProperty(MIME_DECODEPARAMETERS, false);
        decodeParametersStrict = SessionUtil.getBooleanProperty(MIME_DECODEPARAMETERS_STRICT, false);
        encodeParameters = SessionUtil.getBooleanProperty(MIME_ENCODEPARAMETERS, false);
    }

    public int size() {
        return _parameters.size();
    }

    public String get(String name) {
        ParameterValue value = (ParameterValue)_parameters.get(name.toLowerCase());
        if (value != null) {
            return value.value;
        }
        return null;
    }

    public void set(String name, String value) {
        name = name.toLowerCase();
        _parameters.put(name, new ParameterValue(name, value));
    }

    public void remove(String name) {
        _parameters.remove(name);
    }

    public Enumeration getNames() {
        return Collections.enumeration(_parameters.keySet());
    }

    public String toString() {
        // we need to perform folding, but out starting point is 0.
        return toString(0);
    }

    public String toString(int used) {
        StringBuffer stringValue = new StringBuffer();

        Iterator values = _parameters.values().iterator();

        while (values.hasNext()) {
            ParameterValue parm = (ParameterValue)values.next();
            // get the values we're going to encode in here.
            String name = parm.getEncodedName();
            String value = parm.toString();

            // add the semicolon separator.  We also add a blank so that folding/unfolding rules can be used.
            stringValue.append("; ");
            used += 2;

            // too big for the current header line?
            if ((used + name.length() + value.length() + 1) > HEADER_SIZE_LIMIT) {
                // and a CRLF-whitespace combo.
                stringValue.append("\r\n ");
                // reset the counter for a fresh line
                used = 3;
            }
            // now add the keyword/value pair.
            stringValue.append(name);
            stringValue.append("=");

            used += name.length() + 1;

            // we're not out of the woods yet.  It is possible that the keyword/value pair by itself might
            // be too long for a single line.  If that's the case, the we need to fold the value, if possible
            if (used + value.length() > HEADER_SIZE_LIMIT) {
                String foldedValue = ASCIIUtil.fold(used, value);

                stringValue.append(foldedValue);

                // now we need to sort out how much of the current line is in use.
                int lastLineBreak = foldedValue.lastIndexOf('\n');

                if (lastLineBreak != -1) {
                    used = foldedValue.length() - lastLineBreak + 1;
                }
                else {
                    used += foldedValue.length();
                }
            }
            else {
                // no folding required, just append.
                stringValue.append(value);
                used += value.length();
            }
        }

        return stringValue.toString();
    }


    /**
     * Utility class for representing parameter values in the list.
     */
    class ParameterValue {
        public String name;              // the name of the parameter
        public String value;             // the original set value
        public String encodedValue;      // an encoded value, if encoding is requested.

        public ParameterValue(String name, String value) {
            this.name = name;
            this.value = value;
            this.encodedValue = null;
        }

        public ParameterValue(String name, String value, String encodedValue) {
            this.name = name;
            this.value = value;
            this.encodedValue = encodedValue;
        }

        public String toString() {
            if (encodedValue != null) {
                return MimeUtility.quote(encodedValue, HeaderTokenizer.MIME);
            }
            return MimeUtility.quote(value, HeaderTokenizer.MIME);
        }

        public String getEncodedName() {
            if (encodedValue != null) {
                return name + "*";
            }
            return name;
        }
    }
}
