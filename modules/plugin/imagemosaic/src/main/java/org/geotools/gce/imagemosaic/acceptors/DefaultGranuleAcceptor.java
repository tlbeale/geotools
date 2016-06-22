/*
 * GeoTools - The Open Source Java GIS Toolkit
 * http://geotools.org
 *
 * (C) 2016, Open Source Geospatial Foundation (OSGeo)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package org.geotools.gce.imagemosaic.acceptors;

import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.gce.imagemosaic.ImageMosaicConfigHandler;
import org.geotools.gce.imagemosaic.ImageMosaicEventHandlers;
import org.geotools.gce.imagemosaic.MosaicConfigurationBean;
import org.geotools.gce.imagemosaic.RasterManager;
import org.geotools.gce.imagemosaic.Utils;
import org.geotools.gce.imagemosaic.catalogbuilder.CatalogBuilderConfiguration;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Default predicate for accepting/rejecting granules in an image mosaic
 */
public class DefaultGranuleAcceptor implements GranuleAcceptor {
    @Override
    public boolean accepts(GridCoverage2DReader coverage, String coverageName,
            File fileBeingProcessed, ImageMosaicConfigHandler mosaicConfigHandler)
            throws IOException {
        String targetCoverageName = mosaicConfigHandler
                .getTargetCoverageName(coverage, coverageName);
        MosaicConfigurationBean config = mosaicConfigHandler.getConfigurations().get(
                targetCoverageName);

        if (config != null) {
            RasterManager rasterManager =
                    mosaicConfigHandler.getRasterManagerForTargetCoverage(targetCoverageName);
            return checkCRS(coverage, config, coverageName) && checkColorModel(coverage, config, rasterManager, coverageName);
        }
        else {
            //can't validate with empty configuration. usually this means we have a brand new mosaic
            //or the structured coverage doesn't exist in the mosaic yet, meaning downstream code
            //will create it
            return true;
        }
    }

    private boolean checkColorModel(GridCoverage2DReader coverage, MosaicConfigurationBean config,
            RasterManager rasterManager, String inputCoverageName)
            throws IOException
    {
        byte[][] palette = config.getPalette();
        ColorModel colorModel = config.getColorModel();
        ColorModel actualCM = coverage.getImageLayout(inputCoverageName).getColorModel(null);
        if (colorModel == null) {
            colorModel = rasterManager.getDefaultCM();
        }
        if (palette == null) {
            palette = rasterManager.getDefaultPalette();
        }
        return !Utils.checkColorModels(colorModel, palette, actualCM);
    }

    private boolean checkCRS(GridCoverage2DReader coverage, MosaicConfigurationBean config, String inputCoverageName) {
        CoordinateReferenceSystem expectedCRS = config.getCrs();
        CoordinateReferenceSystem actualCRS = coverage.getCoordinateReferenceSystem(inputCoverageName);

        return CRS.equalsIgnoreMetadata(expectedCRS, actualCRS);
    }
}
