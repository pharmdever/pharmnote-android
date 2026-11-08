package com.pharmnote.study;
import org.junit.Test;
import static org.junit.Assert.*;
public class PdfViewportTest {
 @Test public void landscapePageSupportsZoomAndBothPanAxes(){PdfViewport v=new PdfViewport();v.size(1080,1700);v.page(842,595);assertEquals(1080,v.pageWidth*v.scale,.01);v.zoom(4,540,850);float beforeX=v.x,beforeY=v.y;v.pan(-150,-100);assertTrue(v.x<beforeX);assertTrue(v.y<beforeY);v.pan(-100000,-100000);assertEquals(v.width-v.pageWidth*v.scale,v.x,.01);assertEquals(v.height-v.pageHeight*v.scale,v.y,.01);}
 @Test public void pinchKeepsFocusStationaryWhenNotAtBoundary(){PdfViewport v=new PdfViewport();v.size(1080,1700);v.page(600,900);v.zoom(3,540,850);float px=(400-v.x)/v.scale,py=(700-v.y)/v.scale;v.zoom(1.2f,400,700);assertEquals(px,(400-v.x)/v.scale,.01);assertEquals(py,(700-v.y)/v.scale,.01);}
 @Test public void rotationAndFitPageKeepBoundsValid(){PdfViewport v=new PdfViewport();v.size(1080,1800);v.page(842,595);v.zoom(2,540,900);v.size(1800,1080);assertTrue(v.scale>=v.fitPage());v.resetPage();assertTrue(v.pageWidth*v.scale<=v.width+.01);assertTrue(v.pageHeight*v.scale<=v.height+.01);assertTrue(v.x>=0);assertTrue(v.y>=0);}
}
