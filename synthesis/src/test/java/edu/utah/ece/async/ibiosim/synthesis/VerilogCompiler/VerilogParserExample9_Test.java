package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VerilogConstructs.AbstractVerilogConstruct;
import VerilogConstructs.VerilogAssignment;
import VerilogConstructs.VerilogBlock;
import VerilogConstructs.VerilogConditional;
import VerilogConstructs.VerilogDelay;
import VerilogConstructs.VerilogInitialBlock;
import VerilogConstructs.VerilogModule;
import VerilogConstructs.VerilogModuleInstance;
import VerilogConstructs.VerilogWait;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParserExample9_Test {

	private static VerilogModule verilogModule;
	
	@BeforeClass
	public static void setupTest() {
		String[] cmd = {"-v", CompilerTestSuite.verilogDelay_file};
		VerilogCompiler compiledVerilog = CompilerTestSuite.testEnv.runCompiler(cmd); 
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(1, moduleList.size());
		verilogModule = moduleList.get("delay");
		Assert.assertNotNull(verilogModule);
	}
	
	@Test
	public void TestVerilog_inputs() {
		List<String> actual_inputPorts = verilogModule.getInputPorts();
		Assert.assertNotNull(actual_inputPorts);
		Assert.assertTrue(actual_inputPorts.size() == 0);
	}
	
	@Test
	public void TestVerilog_outputs() {
		List<String> actual_outputPorts = verilogModule.getOutputPorts();
		Assert.assertNotNull(actual_outputPorts);
		Assert.assertTrue(actual_outputPorts.size() == 2);
		Assert.assertEquals("out0", actual_outputPorts.get(0));
		Assert.assertEquals("out1", actual_outputPorts.get(1));
	}
	
	@Test
	public void TestVerilog_registers() {
		List<String> actual_registers = verilogModule.getRegisters();
		Assert.assertNotNull(actual_registers);
		Assert.assertTrue(actual_registers.size() == 1);
		Assert.assertEquals("next", actual_registers.get(0));
	}
	
	@Test
	public void TestVerilog_submodules() {
		List<VerilogModuleInstance> actual_submodules = verilogModule.getSubmodules();
		Assert.assertNotNull(actual_submodules);
		Assert.assertTrue(actual_submodules.size() == 0);
	}
	
	@Test
	public void TestVerilog_initialBlocks() {
		List<VerilogInitialBlock> actual_initBlocks = verilogModule.getInitialBlockList();
		Assert.assertNotNull(actual_initBlocks);
		Assert.assertTrue(actual_initBlocks.size() == 0);
	}
	
	@Test
	public void TestVerilog_alwaysBlocks() {
		Assert.assertEquals(1, verilogModule.getNumAlwaysBlock());
		VerilogBlock alwaysBlock = verilogModule.getAlwaysBlock(0);
		Assert.assertNotNull(alwaysBlock);
		
		List<AbstractVerilogConstruct> alwaysConstructs = alwaysBlock.getBlockConstructs();
		Assert.assertEquals(3, alwaysConstructs.size());
	}
	
	@Test
	public void TestVerilog_construct1() {
		VerilogBlock block = verilogModule.getAlwaysBlock(0);
		AbstractVerilogConstruct actual_construct = VerilogTestUtility.getBlockConstruct(block, 0);
		Assert.assertNotNull(actual_construct);
		Assert.assertTrue(actual_construct instanceof VerilogDelay);
		
		VerilogDelay actual_delay = (VerilogDelay) actual_construct;
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void TestVerilog_construct2() {
		VerilogBlock block = verilogModule.getAlwaysBlock(0);
		AbstractVerilogConstruct actual_construct = VerilogTestUtility.getBlockConstruct(block, 1);
		Assert.assertNotNull(actual_construct);
		Assert.assertTrue(actual_construct instanceof VerilogAssignment);
		
		VerilogAssignment actual_assignment = (VerilogAssignment) actual_construct;
		Assert.assertEquals("piecewise(1, uniform(0,1)  < 0.5, 0)", actual_assignment.getExpression());
		Assert.assertEquals("next", actual_assignment.getVariable());
	}
	
	@Test
	public void TestVerilog_construct3() {
		VerilogBlock block = verilogModule.getAlwaysBlock(0);
		AbstractVerilogConstruct always_cst = VerilogTestUtility.getBlockConstruct(block, 2);
		Assert.assertNotNull(always_cst);
		
		VerilogConditional if_ = VerilogTestUtility.getConditionalConstruct(always_cst);
		Assert.assertEquals("eq(next,0)", if_.getIfCondition());
		
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_);
		Assert.assertEquals(2, if_block.getNumConstructSize());
		AbstractVerilogConstruct act_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogAssignment actual_assignment = VerilogTestUtility.getVerilogAssignment(act_cst);
		Assert.assertEquals("out0", actual_assignment.getVariable());
		Assert.assertEquals("1", actual_assignment.getExpression());
		
		act_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogWait waitCondition = VerilogTestUtility.getWaitConstruct(act_cst);
		Assert.assertEquals("eq(out0,1)",waitCondition.getWaitExpression());	
		
		AbstractVerilogConstruct waitConstruct = waitCondition.getDelayConstruct();
		Assert.assertNotNull(waitConstruct);
		VerilogDelay delay = (VerilogDelay) waitConstruct;
		Assert.assertEquals("5", delay.getDelayValue());
		
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_.getElseBlock());
		Assert.assertEquals(2, else_block.getNumConstructSize());
		
		act_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		actual_assignment = VerilogTestUtility.getVerilogAssignment(act_cst);
		Assert.assertEquals("out1", actual_assignment.getVariable());
		Assert.assertEquals("1", actual_assignment.getExpression());
		
		act_cst = VerilogTestUtility.getBlockConstruct(else_block, 1);
		waitCondition = VerilogTestUtility.getWaitConstruct(act_cst);
		Assert.assertEquals("eq(out1,1)",waitCondition.getWaitExpression());	
		
		waitConstruct = waitCondition.getDelayConstruct();
		Assert.assertNotNull(waitConstruct);
		delay = (VerilogDelay) waitConstruct;
		Assert.assertEquals("5", delay.getDelayValue());
	}
	
	
}