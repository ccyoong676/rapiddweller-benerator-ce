/*
 * (c) Copyright 2006-2020 by rapiddweller GmbH & Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General Public License.
 *
 * For redistributing this software or a derivative work under a license other
 * than the GPL-compatible Free Software License as defined by the Free
 * Software Foundation or approved by OSI, you must first obtain a commercial
 * license to this software product from rapiddweller GmbH & Volker Bergmann.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED CONDITIONS,
 * REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE
 * HEREBY EXCLUDED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.rapiddweller.benerator.engine.parser.xml;

import static com.rapiddweller.benerator.engine.DescriptorConstants.*;

import com.rapiddweller.benerator.engine.BeneratorContext;
import com.rapiddweller.benerator.engine.BeneratorRootStatement;
import com.rapiddweller.benerator.engine.ResourceManager;
import com.rapiddweller.benerator.engine.Statement;
import com.rapiddweller.benerator.engine.statement.BeanStatement;
import com.rapiddweller.benerator.engine.statement.IfStatement;
import com.rapiddweller.commons.CollectionUtil;
import com.rapiddweller.commons.ConfigurationError;
import com.rapiddweller.commons.ConversionException;
import com.rapiddweller.commons.ParseException;
import com.rapiddweller.commons.StringUtil;
import com.rapiddweller.commons.xml.XMLUtil;
import com.rapiddweller.script.Assignment;
import com.rapiddweller.script.BeanSpec;
import com.rapiddweller.script.DatabeneScriptParser;
import com.rapiddweller.script.Expression;
import com.rapiddweller.script.expression.ExpressionUtil;
import com.rapiddweller.script.expression.BeanConstruction;
import com.rapiddweller.script.expression.DefaultConstruction;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Element;

/**
 * Parses a &lt;bean&gt; element.<br/><br/>
 * Created: 25.10.2009 01:09:59
 * @since 0.6.0
 * @author Volker Bergmann
 */
public class BeanParser extends AbstractBeneratorDescriptorParser {
	
	private static final Logger logger =  LogManager.getLogger(BeanParser.class);
	
	public BeanParser() {
	    super(EL_BEAN, CollectionUtil.toSet(ATT_ID), CollectionUtil.toSet(ATT_CLASS, ATT_SPEC), 
	    		BeneratorRootStatement.class, IfStatement.class); 
	    // only allowed in non-loop statements in order to avoid leaks
    }

	@Override
	public BeanStatement doParse(Element element, Statement[] parentPath, BeneratorParseContext context) {
		try {
			String id = element.getAttribute(ATT_ID);
			ResourceManager resourceManager = context.getResourceManager();
			Expression<?> bean = parseBeanExpression(element);
			return new BeanStatement(id, bean, resourceManager);
		} catch (ConversionException e) {
			throw new ConfigurationError(e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
    public static Expression<?> parseBeanExpression(Element element) {
		String id = element.getAttribute(ATT_ID);
        Expression<?> instantiation = null;
        String beanSpec = element.getAttribute(ATT_SPEC);
        String beanClass = element.getAttribute(ATT_CLASS);
        if (!StringUtil.isEmpty(beanSpec)) {
        	try {
		        instantiation = DatabeneScriptParser.parseBeanSpec(beanSpec);
        	} catch (ParseException e) {
        		throw new ConfigurationError("Error parsing bean spec: " + beanSpec, e);
        	}
        } else if (!StringUtil.isEmpty(beanClass)) {
	        logger.debug("Instantiating bean of class " + beanClass + " (id=" + id + ")");
	        instantiation = new DefaultConstruction(beanClass);
        } else
        	syntaxError("bean definition is missing 'class' or 'spec' attribute", element);
        Element[] propertyElements = XMLUtil.getChildElements(element, false, EL_PROPERTY);
		Assignment[] propertyInitializers = mapPropertyDefinitions(propertyElements);
        return new BeanConstruction(instantiation, propertyInitializers);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static BeanSpec resolveBeanExpression(Element element, BeneratorContext context) {
		String id = element.getAttribute(ATT_ID);
        Expression<?> instantiation;
        String beanSpecString = element.getAttribute(ATT_SPEC);
        String beanClass = element.getAttribute(ATT_CLASS);
        boolean ref = false;
        if (!StringUtil.isEmpty(beanSpecString)) {
        	try {
		        BeanSpec spec = DatabeneScriptParser.resolveBeanSpec(beanSpecString, context);
				instantiation = ExpressionUtil.constant(spec.getBean());
				ref = spec.isReference();
        	} catch (ParseException e) {
        		throw new ConfigurationError("Error parsing bean spec: " + beanSpecString, e);
        	}
        } else if (!StringUtil.isEmpty(beanClass)) {
	        logger.debug("Instantiating bean of class " + beanClass + " (id=" + id + ")");
	        instantiation = new DefaultConstruction<>(beanClass);
        } else
        	throw new ConfigurationError("Syntax error in definition of bean " + id);
        Element[] propertyElements = XMLUtil.getChildElements(element);
        for (Element propertyElement : propertyElements)
        	if (!EL_PROPERTY.equals(propertyElement.getNodeName()))
        		syntaxError("not a supported bean child element: <" + propertyElement.getNodeName() + ">", 
        				propertyElement);
		Assignment[] propertyInitializers = mapPropertyDefinitions(propertyElements);
		Object result = new BeanConstruction(instantiation, propertyInitializers).evaluate(context);
        return new BeanSpec(result, ref);
	}

	public static Assignment[] mapPropertyDefinitions(Element[] propertyElements) {
		Assignment[] assignments = new Assignment[propertyElements.length];
        for (int i = 0; i < propertyElements.length; i++)
        	assignments[i] = parseProperty(propertyElements[i]);
        return assignments;
    }

	private static Assignment parseProperty(Element propertyElement) {
	    String propertyName = propertyElement.getAttribute("name");
	    Expression<?> value = SettingParser.parseValue(propertyElement);
	    return new Assignment(propertyName, value);
    }

}