/**
 *
 * Copyright 2003-2006 The Apache Software Foundation
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

package javax.mail;

import java.util.ArrayList;
import java.util.List;

/**
 * A FetchProfile defines a list of message attributes that a client wishes to prefetch
 * from the server during a fetch operation.
 *
 * Clients can either specify individual headers, or can reference common profiles
 * as defined by {@link FetchProfile.Item FetchProfile.Item}.
 *
 * @version $Rev$ $Date$
 */
public class FetchProfile {
    /**
     * Inner class that defines sets of headers that are commonly bundled together
     * in a FetchProfile.
     */
    public static class Item {
        /**
         * Item for fetching information about the content of the message.
         *
         * This includes all the headers about the content including but not limited to:
         * Content-Type, Content-Disposition, Content-Description, Size and Line-Count
         */
        public static final Item CONTENT_INFO = new Item("Content-Info");

        /**
         * Item for fetching information about the envelope of the message.
         *
         * This includes all the headers comprising the envelope including but not limited to:
         * From, To, Cc, Bcc, Reply-To, Subject and Date
         *
         * For IMAP4, this should also include the ENVELOPE data item.
         *
         */
        public static final Item ENVELOPE = new Item("Envelope");

        /**
         * Item for fetching information about message flags.
         * Generall corresponds to the X-Flags header.
         */
        public static final Item FLAGS = new Item("Flags");

        protected Item(String name) {
            // hmmm, name is passed in but we are not allowed to provide accessors
            // or to override equals/hashCode so what use is it?
        }
    }

    // use Lists as we don't expect contains to be called often and the number of elements should be small
    private final List items = new ArrayList();
    private final List headers = new ArrayList();

    /**
     * Add a predefined profile of headers.
     *
     * @param item the profile to add
     */
    public void add(Item item) {
        items.add(item);
    }

    /**
     * Add a specific header.
     * @param header the header whose value should be prefetched
     */
    public void add(String header) {
        headers.add(header);
    }

    /**
     * Determine if the given profile item is already included.
     * @param item the profile to check for
     * @return true if the profile item is already included
     */
    public boolean contains(Item item) {
        return items.contains(item);
    }

    /**
     * Determine if the specified header is already included.
     * @param header the header to check for
     * @return true if the header is already included
     */
    public boolean contains(String header) {
        return headers.contains(header);
    }

    /**
     * Get the profile items already included.
     * @return the items already added to this profile
     */
    public Item[] getItems() {
        return (Item[]) items.toArray(new Item[items.size()]);
    }

    /** Get the headers that have already been included.
     * @return the headers already added to this profile
     */
    public String[] getHeaderNames() {
        return (String[]) headers.toArray(new String[headers.size()]);
    }
}
