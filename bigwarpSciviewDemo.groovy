#@ Dataset (label="moving image") mvgImg
#@ Dataset (label="target image") tgtImg
#@ File (label="landmarks file csv", required=false ) landmarksFile
#@ SciView sciView
#@ OpService opService

// Script written by John Bogovic (https://github.com/bogovicj) and Kyle Harrington (https://kyleharrington.com)

import bdv.util.*;
import bdv.viewer.*;
import net.imagej.*;

import bigwarp.*;

import net.imglib2.*;
import net.imglib2.util.*;
import net.imglib2.type.numeric.*;
import net.imglib2.realtransform.*;

import sc.iview.vector.*;
import cleargl.GLVector;

/**
 * Build a bdv source from a imagej2 Dataset
 */
def srcFromDataset( Dataset data )
{

	axisScales = new double[ 3 ];
	for (i in (0..2)){
		axisScales[ i ] = mvgImg.axis( i ).calibratedValue( 1.0 );
	}

	srcTransform = new AffineTransform3D();
	srcTransform.set(
			axisScales[0], 0, 0, 0,
			0, axisScales[1], 0, 0,
			0, 0, axisScales[2], 0 );

	println( srcTransform )
	return new RandomAccessibleIntervalSource(
		data, (NumericType)Util.getTypeFromInterval( data ),
		srcTransform, data.getName());
}

sciView.getFloor().setVisible(false)

mvgImg = opService.convert().uint8(mvgImg)
tgtImg = opService.convert().uint8(tgtImg)

// make bigwarp sources from the dataset
mvgSrcList = [ srcFromDataset( mvgImg )] as Source[];
tgtSrcList = [ srcFromDataset( tgtImg )] as Source[];

// set up bigwarp
bw = new BigWarp( BigWarpInit.createBigWarpData( mvgSrcList, tgtSrcList, null ), "bigwarp", null );

mvgImg2 = opService.image().normalize( mvgImg )
mvgVolume = sciView.addVolume( mvgImg2 );
mvgVolume.setPixelToWorldRatio(0.1f);
mvgVolume.setDirty(true);
mvgVolume.setNeedsUpdate(true);

tgtImg2 = opService.image().normalize( tgtImg )
tgtVolume = sciView.addVolume( tgtImg );
tgtVolume.setPixelToWorldRatio(0.1f);
tgtVolume.setDirty(true);
tgtVolume.setNeedsUpdate(true);

rampMin = 0f
rampMax = 0.1f

tf = mvgVolume.getTransferFunction();
tf.clear();
tf.addControlPoint(0.0f, 0.0f);
tf.addControlPoint(rampMin, 0.0f);
tf.addControlPoint(1.0f, rampMax);

tf = mvgVolume.getTransferFunction();
tf.clear();
tf.addControlPoint(0.0f, 0.0f);
tf.addControlPoint(rampMin, 0.0f);
tf.addControlPoint(1.0f, rampMax);

// load landmarks if there are any
if( landmarksFile != null )
	bw.getLandmarkPanel().getTableModel().load( landmarksFile );

calibrationScale = mvgImg.axis( 1 ).calibratedValue( 1.0 );// Note this is hard coded to use dimension 1

// loop through landmarks
landmarkTable = bw.getLandmarkPanel().getTableModel();
for (i in (0..<landmarkTable.getRowCount()))
{
	println( i )
	mvgImagePoint = landmarkTable.getMovingPoint( i )
	tgtImagePoint = landmarkTable.getFixedPoint( i )
	scale = mvgVolume.getPixelToWorldRatio()
	ox = mvgVolume.getSizeX()/2.0
	oy = mvgVolume.getSizeY()/2.0
	oz = mvgVolume.getSizeZ()/2.0
	mvgNode = sciView.addSphere( new ClearGLVector3( (float)((mvgImagePoint[0]/calibrationScale - ox) * scale), (float)((mvgImagePoint[1]/calibrationScale - oy) * scale), (float)((mvgImagePoint[2]/calibrationScale - oz) * scale) ), 1 )
	mvgNode.setScale(new GLVector(mvgVolume.getPixelToWorldRatio(),
                    mvgVolume.getPixelToWorldRatio(),
                    mvgVolume.getPixelToWorldRatio()))
	// set node color
	tgtNode = sciView.addSphere( new ClearGLVector3( (float)((tgtImagePoint[0]/calibrationScale - ox)* scale), (float)((tgtImagePoint[1]/calibrationScale - oy)* scale), (float)((tgtImagePoint[2]/calibrationScale - oz)* scale) ), 2 )
	tgtNode.setScale(new GLVector(tgtVolume.getPixelToWorldRatio(),
                    tgtVolume.getPixelToWorldRatio(),
                    tgtVolume.getPixelToWorldRatio()))
	println( mvgImagePoint )
	println( tgtImagePoint )
	println( ' ' )
}
