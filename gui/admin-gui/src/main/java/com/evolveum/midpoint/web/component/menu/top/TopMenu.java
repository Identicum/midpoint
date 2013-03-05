/*
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.web.component.menu.top;

import com.evolveum.midpoint.web.component.util.VisibleEnableBehaviour;
import org.apache.commons.lang.Validate;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;

import java.util.List;

public class TopMenu extends Panel {

    private List<TopMenuItem> topItems;
    private List<BottomMenuItem> bottomItems;

    public TopMenu(String id, List<TopMenuItem> topItems, List<BottomMenuItem> bottomItems) {
        super(id);
        Validate.notNull(topItems, "List with top menu topItems must not be null.");
        Validate.notNull(bottomItems, "List with top menu bottomItems must not be null.");
        this.topItems = topItems;
        this.bottomItems = bottomItems;

        add(new Loop("topList", topItems.size()) {

            @Override
            protected void populateItem(LoopItem loopItem) {
                final TopMenuItem item = TopMenu.this.topItems.get(loopItem.getIndex());
                BookmarkablePageLink<String> link = new BookmarkablePageLink<String>("topLink", item.getPage());
                if (loopItem.getIndex() == 0) {
                    link.add(new AttributeAppender("class", new Model("first"), " "));
                } else if (loopItem.getIndex() == (TopMenu.this.topItems.size() - 1)) {
                    link.add(new AttributeAppender("class", new Model("last"), " "));
                }

                link.add(new Label("topLabel", new StringResourceModel(item.getLabel(), TopMenu.this, null)));
                link.add(new Label("topDescription", new StringResourceModel(item.getDescription(), TopMenu.this, null)));

                Page page = TopMenu.this.getPage();
                if (page != null && item.getMarker().isAssignableFrom(page.getClass())) {
                    link.add(new AttributeAppender("class", new Model("selected-top"), " "));
                }
                loopItem.add(link);
            }
        });

        Loop bottomLoop = new Loop("bottomList", bottomItems.size()) {

            @Override
            protected void populateItem(LoopItem loopItem) {
                final BottomMenuItem item = TopMenu.this.bottomItems.get(loopItem.getIndex());
                BookmarkablePageLink<String> link = new BookmarkablePageLink<String>("bottomLink", item.getPage());
                link.add(new Label("bottomLabel", item.getLabel()));

                Page page = TopMenu.this.getPage();
                if (page != null && page.getClass().isAssignableFrom(item.getPage())) {
                    link.add(new AttributeAppender("class", new Model("selected-bottom"), " "));
                }
                loopItem.add(link);

                if (item.getVisible() != null) {
                    loopItem.add(item.getVisible());
                }
            }
        };

        bottomLoop.add(new VisibleEnableBehaviour() {

            @Override
            public boolean isVisible() {
                return !TopMenu.this.bottomItems.isEmpty();
            }
        });
        add(bottomLoop);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(new PackageResourceReference(TopMenu.class, "TopMenu.css")));
    }
}
