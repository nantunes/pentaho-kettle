/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepMeta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TransMetaIntegrationTest {

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  /**
   * This test was created after <a href="http://jira.pentaho.com/browse/PDI-13634">PDI-13634</a>. It verifies the process
   * of previewing field in a transformation, that contains a {@linkplain org.pentaho.di.trans.steps.mapping.Mapping
   * Mapping step}.
   * <p/>
   * The input transformation is a simplified version of the one, attached to the ticket. It has a linear structure:
   * <tt>generator -> mapper -> captor</tt>. <tt>mapper</tt> invokes the internal transformation, that has the following
   * structure: <pre>
   *   input -> dummy1 ---->
   *              |         |
   *            dummy2 -> lookup -> output
   * </pre>
   *
   * @author Andrey Khayrutdinov
   */
  @Test
  public void shouldNotShareRowMetaInterfaceAmongDifferentSteps() throws Exception {
    TransMeta transMeta = new TransMeta( "testfiles/org/pentaho/di/trans/pdi-13634.ktr" );
    StepMeta mapper = transMeta.findStep( "mapper" );
    StepMeta captor = transMeta.findStep( "captor" );

    RowMetaInterface mapperOutputPreview = transMeta.getStepFields( mapper, captor, null );
    assertPreviewRows( mapperOutputPreview );

    RowMetaInterface captorInputPreview = transMeta.getPrevStepFields( captor, null );
    assertPreviewRows( captorInputPreview );
  }

  /**
   * Checks if <tt>preview</tt> contains the proper data. For the input example, it is two String fields: <tt>value</tt>
   * and <tt>new_value</tt>.
   *
   * @param preview previewed fields
   */
  private static void assertPreviewRows( RowMetaInterface preview ) {
    assertEquals( "There should be two fields", 2, preview.size() );
    assertEquals( "The first should be 'value'", "value", preview.getValueMeta( 0 ).getName() );
    assertEquals( "The second should be 'new_value'", "new_value", preview.getValueMeta( 1 ).getName() );
  }

  /**
   * This test was created after <a href="http://jira.pentaho.com/browse/PPP-3405">PPP-3405</a>
   * {@link org.pentaho.di.trans.TransMeta} should load all database connection and add to private list all non shared
   * 
   * @throws KettleXMLException
   * @throws KettleMissingPluginsException
   */
  @Test
  public void testLoadXML() throws KettleXMLException, KettleMissingPluginsException {
    TransMeta meta = new TransMeta( "testfiles/org/pentaho/di/trans/transMetaTest.ktr" );
    assertFalse( meta.getPrivateTransformationDatabases().isEmpty() );
    assertTrue( meta.getPrivateTransformationDatabases().get( 0 ).equalsIgnoreCase( "test" ) );
    assertTrue( meta.getPartitionSchemasNames()[0].equalsIgnoreCase( "test" ) );
    assertTrue( meta.getSlaveServerNames()[0].equalsIgnoreCase( "test" ) );
    assertTrue( meta.getClusterSchemaNames()[0].equalsIgnoreCase( "test" ) );
    assertTrue( meta.getDatabaseNames()[0].equalsIgnoreCase( "shared" ) );
    assertTrue( meta.getDatabases().get( 0 ).isShared() );
    assertTrue( meta.getDatabaseNames()[1].equalsIgnoreCase( "test" ) );
    assertFalse( meta.getDatabases().get( 1 ).isShared() );
  }
}
