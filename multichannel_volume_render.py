#@ SciView sciView
#@ IOService ioService
#@ UIService uiService
#@ LUTService lutService
#@ CommandService commandService
#@ File blueCh
#@ File redCh
#@ File greenCh
#@ Float xResolution
#@ Float yResolution
#@ Float zResolution

# Note: as of 2019-10-07, this requires SciView-Unstable

blue_fn = blueCh.getAbsolutePath()
red_fn = redCh.getAbsolutePath()
green_fn = greenCh.getAbsolutePath()

blue = ioService.open(blue_fn)
green = ioService.open(green_fn)
red = ioService.open(red_fn)

uiService.show(blue)

from cleargl import GLVector

vScale = GLVector( [xResolution*10, yResolution*10, zResolution*10] )

nBlue = sciView.addVolume(blue, [xResolution, yResolution, zResolution])
nGreen = sciView.addVolume(green, [xResolution, yResolution, zResolution])
nRed = sciView.addVolume(red, [xResolution, yResolution, zResolution])

import time

time.sleep(2)

redCT = lutService.loadLUT( lutService.findLUTs().get( "Cyan.lut" ) )
greenCT = lutService.loadLUT( lutService.findLUTs().get( "Magenta.lut" ) )
blueCT = lutService.loadLUT( lutService.findLUTs().get( "Yellow.lut" ) )

sciView.setColormap( nRed, redCT )
sciView.setColormap( nGreen, greenCT )
sciView.setColormap( nBlue, blueCT )

sciView.getFloor().setVisible(False)

from graphics.scenery.volumes import TransferFunction

commandService.run('sc.iview.commands.view.SetTransferFunction',True,['volume',nRed,'rampMin',0,'rampMax',0.01])
commandService.run('sc.iview.commands.view.SetTransferFunction',True,['volume',nGreen,'rampMin',0,'rampMax',0.1])
commandService.run('sc.iview.commands.view.SetTransferFunction',True,['volume',nBlue,'rampMin',0,'rampMax',0.4])

nBlue.setName('Yellow')
nGreen.setName('Magenta')
nRed.setName('Cyan')

from graphics.scenery import BoundingGrid

bg = BoundingGrid()
bg.setNode(nBlue)

nRed.setScale(vScale)
nGreen.setScale(vScale)
nBlue.setScale(vScale)
