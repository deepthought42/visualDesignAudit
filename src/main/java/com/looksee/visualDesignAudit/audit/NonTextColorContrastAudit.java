package com.looksee.visualDesignAudit.audit;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.looksee.utils.ColorUtils;
import com.looksee.visualDesignAudit.gcp.GoogleCloudStorage;
import com.looksee.visualDesignAudit.models.Audit;
import com.looksee.visualDesignAudit.models.AuditRecord;
import com.looksee.visualDesignAudit.models.ColorContrastIssueMessage;
import com.looksee.visualDesignAudit.models.ColorData;
import com.looksee.visualDesignAudit.models.DesignSystem;
import com.looksee.visualDesignAudit.models.ElementState;
import com.looksee.visualDesignAudit.models.IExecutablePageStateAudit;
import com.looksee.visualDesignAudit.models.PageState;
import com.looksee.visualDesignAudit.models.UXIssueMessage;
import com.looksee.visualDesignAudit.models.enums.AuditCategory;
import com.looksee.visualDesignAudit.models.enums.AuditLevel;
import com.looksee.visualDesignAudit.models.enums.AuditName;
import com.looksee.visualDesignAudit.models.enums.AuditSubcategory;
import com.looksee.visualDesignAudit.models.enums.Priority;
import com.looksee.visualDesignAudit.models.enums.WCAGComplianceLevel;
import com.looksee.visualDesignAudit.models.recommend.ColorContrastRecommendation;
import com.looksee.visualDesignAudit.models.recommend.Recommendation;
import com.looksee.visualDesignAudit.services.AuditService;
import com.looksee.visualDesignAudit.services.UXIssueMessageService;


/**
 * Responsible for executing an audit on the hyperlinks on a page for the information architecture audit category
 */
@Component
public class NonTextColorContrastAudit implements IExecutablePageStateAudit {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(NonTextColorContrastAudit.class);
	
	@Autowired
	private AuditService audit_service;
	
	@Autowired
	private UXIssueMessageService issue_message_service;
	
	/**
	 * {@inheritDoc}
	 * 
	 * Identifies colors used on page, the color scheme type used, and the ultimately the score for how the colors used conform to scheme
	 *  
	 *  WCAG Success Criteria Source - https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html
	 * 
	 * There is no level A compliance
	 * Level AA is the requirement used withiin common laws and standards
	 * Level AAA This is for companies looking to provide an exceptional experience with color contrast
	 * 
	 * Compliance level is determined by the {@link DesignSystem} if it isn't null, otherwise defaults to AAA level
	 * 
	 * @throws MalformedURLException 
	 * @throws URISyntaxException 
	 */
	@Override
	public Audit execute(PageState page_state, AuditRecord audit_record, DesignSystem design_system) {
		assert page_state != null; 
		
		WCAGComplianceLevel wcag_compliance = WCAGComplianceLevel.AAA;
		
		if(design_system != null) {
			wcag_compliance = design_system.getWcagComplianceLevel();
			
			if(wcag_compliance.equals(WCAGComplianceLevel.A)) {
				return null;
			}
		}
		
		//get all button elements
		List<ElementState> non_text_elements = getAllButtons(page_state.getElements());

		non_text_elements.addAll(getAllInputs(page_state.getElements()));
			
		return evaluateNonTextContrast(page_state, non_text_elements, design_system);
	}

	private List<ElementState> getAllIcons(List<ElementState> elements) {
		//identify font awesome icons
		
		return null;
	}

	private List<ElementState> getAllInputs(List<ElementState> elements) {
		return elements.parallelStream()
							.filter(p ->p.getName().equalsIgnoreCase("input"))
							.distinct()
							.collect(Collectors.toList());  // iterating price 

	}

	private List<ElementState> getAllButtons(List<ElementState> elements) {
		return elements.parallelStream()
							.filter(p -> p.getName().equalsIgnoreCase("button") || (p.getAttributes().containsKey("class") && p.getAttribute("class").toLowerCase().contains("button")))
							.distinct()
							.collect(Collectors.toList());  // iterating price 
	}
	
	public Color getPixelColor(String image_url, int x, int y) throws IOException {
		BufferedImage image = GoogleCloudStorage.getImage(image_url);
		return new Color(image.getRGB(x, y));
	}

	/**
	 * Evaluates non text elements for contrast with parent element
	 * 
	 * @param page_state
	 * @param non_text_elements
	 * @param design_system TODO
	 * @return
	 */
	private Audit evaluateNonTextContrast(PageState page_state, List<ElementState> non_text_elements, DesignSystem design_system) {
		assert page_state != null;
		assert non_text_elements != null;
		
		
		Set<UXIssueMessage> issue_messages = new HashSet<>();
		Set<String> labels = new HashSet<>();
		labels.add(AuditCategory.AESTHETICS.toString().toLowerCase());
		labels.add("color contrast");
		labels.add("accessibility");
		labels.add("wcag");
		
		if(page_state.getUrl().contains("apple.com/shop/buy-watch/apple-watch")) {
			log.warn("-------------------------------------------------------------");
			log.warn(non_text_elements.size()+" non-text elements being evaluated");
			log.warn("-------------------------------------------------------------");
		}
		for(ElementState element : non_text_elements) {
			//ColorData font_color = new ColorData(element.getRenderedCssValues().get("color"));
			//get parent element of button
			try {
				//retrieve all elements for page state
				//evaluate each element to see if xpath is a subset of element xpath, keeping the elements with shortest difference
				ColorData parent_bkg = null;
				//List<ElementState> elements = page_state_service.getElementStates(page_state.getKey());
				for(ElementState element_state : page_state.getElements()) {
					if(element_state.getKey().contentEquals(element.getKey())) {
						continue;
					}

					if(element.getXpath().contains(element_state.getXpath())) {

							parent_bkg = new ColorData(element_state.getBackgroundColor());
							break;
					}
				}

				//getting border color
				ColorData element_bkg = new ColorData(element.getBackgroundColor());
				String border_color_rgb = element_bkg.rgb();
				if(element.getRenderedCssValues().get("border-inline-start-width") != "0px") {
					border_color_rgb = element.getRenderedCssValues().get("border-inline-start-color");
				}
				else if(element.getRenderedCssValues().get("border-inline-end-width") != "0px") {
					border_color_rgb = element.getRenderedCssValues().get("border-inline-end-color");
				}
				else if(element.getRenderedCssValues().get("border-block-start-width") != "0px") {
					border_color_rgb = element.getRenderedCssValues().get("border-block-start-color");
				}
				else if(element.getRenderedCssValues().get("border-block-end-width") != "0px") {
					border_color_rgb = element.getRenderedCssValues().get("border-block-end-color");
				}

				ColorData border_color = new ColorData(border_color_rgb);
				
				//if element has border color different than element then set element_bkg to border color
				if(!element.getName().contentEquals("input")
						&& hasContinuousBorder(element) 
						&& !borderColorMatchesBackground(element))
				{
					element_bkg = getBorderColor(element);
				}
				
				/*
				if(parent_bkg == null) {
					parent_bkg = new ColorData("rgb(255,255,255)");
				}
				*/
				parent_bkg = new ColorData(element.getBackgroundColor());
				double contrast = ColorData.computeContrast(parent_bkg, element_bkg);
				double border_contrast = ColorData.computeContrast(parent_bkg, border_color);
				double highest_contrast = 0.0;
				if(contrast > border_contrast) {
					highest_contrast = contrast;
				}
				else {
					highest_contrast = border_contrast;
				}
				
				/*
				element = element_state_service.findById(element.getId());
				element.setNonTextContrast(highest_contrast);
				element = element_state_service.save(element);
				*/
				
				//calculate contrast of button background with background of parent element
				if(highest_contrast < 3.0){
					String title = "Element has low contrast";
					String description = "Element background has low contrast against the surrounding background";
					//no points are rewarded for low contrast
					
					String ada_compliance = "Non-text items should have a minimum contrast ratio of 3:1.";
					
					String recommendation = "use a darker/lighter shade of "+ element.getBackgroundColor() +" to achieve a contrast of 3:1";
					Set<Recommendation> recommendations = generateNonTextContrastRecommendations(element, 
																								 parent_bkg);
					
					ColorContrastIssueMessage low_contrast_issue = new ColorContrastIssueMessage(
																				Priority.HIGH,
																				description,
																				highest_contrast,
																				element_bkg.rgb(),
																				parent_bkg.rgb(),
																				element,
																				AuditCategory.AESTHETICS,
																				labels,
																				ada_compliance,
																				title,
																				null, 
																				0, 
																				1, 
																				recommendation);
					
					low_contrast_issue = issue_message_service.saveColorContrast(low_contrast_issue);
					//issue_message_service.addElement(low_contrast_issue.getId(), element.getId());
					issue_messages.add(low_contrast_issue);
					//MessageBroadcaster.sendIssueMessage(page_state.getId(), low_contrast_issue);
				}
				else {
					String title = "Element contrast is accessisible";
					String description = "Element background has appropriate contrast for accessibility";
					//no points are rewarded for low contrast
					
					String ada_compliance = "Element is compliant with WCAG 2.1 " + design_system.getWcagComplianceLevel() + " standards.";
					
					String recommendation = "";
					Set<Recommendation> recommendations = generateNonTextContrastRecommendations(element, 
																								 parent_bkg);
					
					ColorContrastIssueMessage accessible_contrast = new ColorContrastIssueMessage(
																				Priority.HIGH,
																				description,
																				highest_contrast,
																				element_bkg.rgb(),
																				parent_bkg.rgb(),
																				element,
																				AuditCategory.AESTHETICS,
																				labels,
																				ada_compliance,
																				title,
																				null, 
																				1, 
																				1, 
																				recommendation);
					
					accessible_contrast = issue_message_service.saveColorContrast(accessible_contrast);
					//issue_message_service.addElement(accessible_contrast.getId(), element.getId());
					issue_messages.add(accessible_contrast);
				}
			}
			catch(NullPointerException e) {
				log.warn("null pointer..." + e.getMessage());
				e.printStackTrace();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		} 

		String why_it_matters = "<p>Icons are an easily recognizable, fun element, and a great way to\n" + 
				"communicate with your user beyond just using text. Icons should be\n" + 
				"familiar and captivating.</p>" + 
				"<p>Bright colors have higher conversion rates, so it is important for your\n" + 
				"button to have a high contrast score to create an eye-catching effect\n" + 
				"and be obviously clickable.</p>";

		int points_earned = 0;
		int max_points = 0;
		for(UXIssueMessage issue_msg : issue_messages) {
			points_earned += issue_msg.getPoints();
			max_points += issue_msg.getMaxPoints();
			/*
			if(issue_msg.getScore() < 90 && issue_msg instanceof ElementStateIssueMessage) {
				ElementStateIssueMessage element_issue_msg = (ElementStateIssueMessage)issue_msg;
				List<ElementState> good_examples = audit_service.findGoodExample(AuditName.NON_TEXT_BACKGROUND_CONTRAST, 100);
				if(good_examples.isEmpty()) {
					continue;
				}
				Random random = new Random();
				ElementState good_example = good_examples.get(random.nextInt(good_examples.size()-1));
				element_issue_msg.setGoodExample(good_example);
				issue_message_service.save(element_issue_msg);
			}
			*/
		}
		
		String description = "Color contrast of text";		
		Audit audit = new Audit(AuditCategory.AESTHETICS,
								 AuditSubcategory.COLOR_MANAGEMENT,
								 AuditName.NON_TEXT_BACKGROUND_CONTRAST,
								 points_earned,
								 issue_messages,
								 AuditLevel.PAGE,
								 max_points,
								 page_state.getUrl(),
								 why_it_matters,
								 description,
								 true);

		audit_service.save(audit);
		//audit_service.addAllIssues(audit.getId(), issue_messages);
		return audit;
	}


	private ColorData getBorderColor(ElementState element) {
		return new ColorData(element.getRenderedCssValues().get("border-bottom-color"));
	}


	private boolean borderColorMatchesBackground(ElementState element) {
		String border = element.getRenderedCssValues().get("border-bottom-color");
		String background = element.getRenderedCssValues().get("background-color");
		return border.contentEquals(background);
	}


	private boolean hasContinuousBorder(ElementState element) {
		String bottom = element.getRenderedCssValues().get("border-bottom-color");
		String top = element.getRenderedCssValues().get("border-top-color");
		String left = element.getRenderedCssValues().get("border-left-color");
		String right = element.getRenderedCssValues().get("border-right-color");
		return bottom.contentEquals(top)
				&& top.contentEquals(left)
				&& left.contentEquals(right);
	}
	
	/**
	 * Generates {@link Set} of {@link ColorContrastRecommendation recommendations} based on the text color, background color and font_size
	 * 	NOTE : assumes a light color scheme only. Doesn't currently account for dark color scheme
	 * 
	 * @param font_color
	 * @param background_color
	 * @param font_size
	 * @param is_bold TODO
	 * 
	 * @pre font_color != null
	 * @pre background_color != null
	 * 
	 * @return
	 */
	private Set<Recommendation> generateNonTextContrastRecommendations(ElementState element,
																	 ColorData background_color) {
		assert element != null;
		assert background_color != null;
		
		Set<Recommendation> recommendations = new HashSet<>();
		
		//generate color suggestions with different background color shades (text doesn't change)
		
		boolean is_dark_theme = false;
		//if text is lighter than background then it's dark theme
		//otherwise light theme
		ColorContrastRecommendation recommended_bg_color = ColorUtils.findCompliantNonTextBackgroundColor(new ColorData(element.getBackgroundColor()), 
																											background_color, 
																											is_dark_theme);
		recommendations.add( recommended_bg_color);
		
		
		//generate color suggestions with different text color shades (background doesn't change)
		Set<ColorContrastRecommendation> recommended_font_color = ColorUtils.findCompliantElementColors(element, 
																										background_color, 
																										is_dark_theme);
		recommendations.addAll( recommended_font_color);
		
		
		//generate color suggestions with varying text and background colors that are within a bounded range of the original color
		// NOTE: This involves pushing these values in opposing directions until we find a pair that meets WCAG 2.1 AAA standards. 
		//       Then, the pair of colors are shifted together to find new color pairs
		
		
		return recommendations;
	}
}