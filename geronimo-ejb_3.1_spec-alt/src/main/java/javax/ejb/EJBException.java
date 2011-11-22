/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.ejb;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @version $Rev$ $Date$
 */
public class EJBException extends RuntimeException {

    private static final long serialVersionUID = 796770993296843510L;
    private Exception causeException;

    public EJBException() {
    }

    public EJBException(Exception causeException) {
        this.causeException = causeException;
    }

    public EJBException(String message) {
        super(message);
    }

    public EJBException(String message, Exception causeException) {
        super(message);
        this.causeException = causeException;
    }

    public Exception getCausedByException() {
        return causeException;
    }

    public String getMessage() {

        if (causeException == null) return super.getMessage();

        StringBuilder sb = new StringBuilder();

        if (super.getMessage() != null) {
            sb.append(super.getMessage());
            sb.append("; ");
        }

        sb.append("nested exception is: ");
        sb.append(causeException.toString());

        return sb.toString();
    }

    public void printStackTrace(PrintStream ps) {
        if (causeException == null) {
            super.printStackTrace(ps);
        } else synchronized (ps) {
            ps.println(this);
            causeException.printStackTrace(ps);
            super.printStackTrace(ps);
        }
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(PrintWriter pw) {
        if (causeException == null) {
            super.printStackTrace(pw);
        } else synchronized (pw) {
            pw.println(this);
            causeException.printStackTrace(pw);
            super.printStackTrace(pw);
        }
    }
}
