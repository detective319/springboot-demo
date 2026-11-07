package com.example.demo.wicket;

import com.example.demo.dto.*;
import com.example.demo.service.ProductService;
import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.List;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.*;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.*;

public class HomePage extends WebPage {
    @SpringBean
    private ProductService productService;

    public HomePage() {
        add(new FeedbackPanel("feedback"));
        IModel<List<ProductResponse>> products = new LoadableDetachableModel<>() {
            @Override
            protected List<ProductResponse> load() { return productService.findAll(); }
        };
        add(new ListView<ProductResponse>("products", products) {
            @Override
            protected void populateItem(ListItem<ProductResponse> item) {
                ProductResponse product = item.getModelObject();
                item.add(new Label("id", product.id()));
                item.add(new Label("name", product.name()));
                item.add(new Label("price", product.price().setScale(2).toPlainString()));
            }
        });
        TextField<String> name = new TextField<>("name", Model.of(""));
        name.setRequired(true);
        name.add(StringValidator.maximumLength(100));
        TextField<BigDecimal> price = new TextField<>("price", Model.of(BigDecimal.ZERO), BigDecimal.class);
        price.setRequired(true);
        price.add(RangeValidator.minimum(BigDecimal.ZERO));
        Form<Void> form = new Form<>("form") {
            @Override
            protected void onSubmit() {
                try {
                    productService.create(new ProductRequest(name.getModelObject(), price.getModelObject()));
                    setResponsePage(HomePage.class);
                } catch (ConstraintViolationException exception) {
                    error("商品名稱不可為空白，價格須為非負數、最多十位整數及兩位小數。");
                }
            }
        };
        form.add(name, price);
        add(form);
    }
}
