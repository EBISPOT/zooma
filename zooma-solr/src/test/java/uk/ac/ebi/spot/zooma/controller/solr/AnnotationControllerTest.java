package uk.ac.ebi.spot.zooma.controller.solr;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ebi.spot.zooma.ZoomaSolrApplication;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;



/**
 * Created by olgavrou on 16/05/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ZoomaSolrApplication.class)
@Ignore
public class AnnotationControllerTest {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;
    private RestDocumentationResultHandler document;


    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    @Before
    public void setUp(){

        this.document = document("{method-name}"
                ,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())
        );

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .alwaysDo(this.document)
                .build();
    }

    @Test
    public void findByPropertyValue() throws Exception {
//
//        this.document.document(
//                responseFields(
//                        fieldWithPath("_links").description("<<resources-page-links,Links>> to other resources"),
//                        fieldWithPath("_embedded").description("The list of resources"),
//                        fieldWithPath("page.size").description("The number of resources in this page"),
//                        fieldWithPath("page.totalElements").description("The total number of resources"),
//                        fieldWithPath("page.totalPages").description("The total number of pages"),
//                        fieldWithPath("page.number").description("The page number")
//                ),
//                links(halLinks(),
//                        linkWithRel("self").description("This resource list"),
//                        linkWithRel("first").description("The first page in the resource list"),
//                        linkWithRel("next").description("The next page in the resource list"),
//                        linkWithRel("prev").description("The previous page in the resource list"),
//                        linkWithRel("last").description("The last page in the resource list")
//                )
//
//        );



        this.mockMvc.perform(get("/annotations/search?q={query}&page={page}&size={size}", "cell", 0, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.annotations").isArray())
                .andExpect(jsonPath("_embedded.annotations.[*].propertyType").isNotEmpty())
                .andExpect(jsonPath("_embedded.annotations.[*].propertyValue").isNotEmpty())
                .andExpect(jsonPath("_embedded.annotations.[*].semanticTag").isArray())
                .andDo(this.document.document(responseFields(
                        fieldWithPath("_links").description("<<resources-page-links,Links>> to other resources"),
                        fieldWithPath("_embedded").description("The list of resources"),
                        fieldWithPath("page.size").description("The number of resources in this page"),
                        fieldWithPath("page.totalElements").description("The total number of resources"),
                        fieldWithPath("page.totalPages").description("The total number of pages"),
                        fieldWithPath("page.number").description("The page number")

//                        fieldWithPath("_embedded.annotations").description("annotation summaries list"),
//                        fieldWithPath("_embedded.annotations.[*].propertyType").description("annotation summary property type"),
//                        fieldWithPath("_embedded.annotations.[*].propertyValue").description("annotation summary proprty value"),
//                        fieldWithPath("_embedded.annotations.[*].semanticTag").description("annotation summary's semantic tags"),
//                        fieldWithPath("_embedded.annotations.[*].mongoid").description("annotation summary's list of mongoids of which the annotation summary was comprised from"),
//                        fieldWithPath("_embedded.annotations.[*].strongestMongoid").description("annotation summary's mongoid with strongest quality"),
//                        fieldWithPath("_embedded.annotations.[*].source").description("annotation summary's list of provenance sources"),
//                        fieldWithPath("_embedded.annotations.[*].topic").description("annotation summary's list of provenance topics"),
//                        fieldWithPath("_embedded.annotations.[*].quality").description("annotation summary's list of provenance topics"),

                )))
        ;

    }

    @Test
    @Ignore
    public void findByPropertyValueFilterSources() throws Exception {

    }

    @Test
    @Ignore
    public void findByPropertyValueFilterTopics() throws Exception {

    }

    @Test
    @Ignore
    public void findByPropertyTypeAndValue() throws Exception {

    }

    @Test
    @Ignore
    public void findByPropertyTypeAndValueFilterSources() throws Exception {

    }

    @Test
    @Ignore
    public void findByPropertyTypeAndValueFilterTopics() throws Exception {

    }

    private RestDocumentationResultHandler documentPrettyPrintReqResp(String useCase) {
        return document(useCase,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()));
    }

}